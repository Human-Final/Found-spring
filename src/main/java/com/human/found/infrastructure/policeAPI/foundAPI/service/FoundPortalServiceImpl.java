package com.human.found.infrastructure.policeAPI.foundAPI.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.human.found.infrastructure.policeAPI.foundAPI.vo.FoundPortalApiItemVO;
import com.human.found.infrastructure.policeAPI.foundAPI.vo.FoundPortalApiResponseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoundPortalServiceImpl implements FoundPortalService {

    private static final int NUM_OF_ROWS = 5000;
    private static final int MAX_PAGE = 500;

    private final FoundPortalTxService foundPortalTxService;

    @Value("${portal.api.url}")
    private String apiUrl;

    @Value("${police.api.key}")
    private String serviceKey;

    @Override
    public void saveFoundPortalData() throws Exception {

        long totalStartTime = System.currentTimeMillis();

        RestTemplate restTemplate = new RestTemplate();
        XmlMapper xmlMapper = new XmlMapper();

        int pageNo = 1;

        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6);

        List<FoundPortalApiItemVO> allItems =
                new ArrayList<>();

        /*
         * API 전체 페이지에서 atcId 중복 제거
         */
        Set<String> collectedAtcIds =
                new HashSet<>();

        /*
         * API가 동일한 페이지를 반복해서 반환하는지 검사
         */
        Set<String> collectedPageKeys =
                new HashSet<>();

        long apiStartTime =
                System.currentTimeMillis();

        while (true) {

            if (pageNo > MAX_PAGE) {
                throw new IllegalStateException(
                    "포털기관 습득물 API 요청이 최대 페이지를 초과했습니다. "
                    + "동일 페이지 반복 여부를 확인해야 합니다. "
                    + "pageNo = " + pageNo
                );
            }

            String url = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam(
                    "START_YMD",
                    sixMonthsAgo.format(
                        DateTimeFormatter.BASIC_ISO_DATE
                    )
                )
                .queryParam(
                    "END_YMD",
                    today.format(
                        DateTimeFormatter.BASIC_ISO_DATE
                    )
                )
                .build(false)
                .toUriString();

            System.out.println(
                "포털기관 습득물 API 요청 시작"
                + " / pageNo = " + pageNo
            );

            long pageStartTime =
                    System.currentTimeMillis();

            byte[] bytes =
                    getBytesWithRetry(
                        restTemplate,
                        url,
                        pageNo
                    );

            long pageEndTime =
                    System.currentTimeMillis();

            if (bytes == null || bytes.length == 0) {
                throw new IllegalStateException(
                    "포털기관 습득물 API 응답이 없습니다. "
                    + "기존 데이터는 변경하지 않습니다. "
                    + "pageNo = " + pageNo
                );
            }

            String xml =
                    new String(
                        bytes,
                        StandardCharsets.UTF_8
                    );

            FoundPortalApiResponseVO response =
                    xmlMapper.readValue(
                        xml,
                        FoundPortalApiResponseVO.class
                    );

            if (response == null ||
                response.getBody() == null ||
                response.getBody().getItems() == null ||
                response.getBody()
                        .getItems()
                        .getItem() == null) {

                if (pageNo == 1) {
                    throw new IllegalStateException(
                        "포털기관 습득물 API 첫 페이지에 "
                        + "item 데이터가 없습니다."
                    );
                }

                System.out.println(
                    "포털기관 습득물 item 없음. 수집 종료"
                );

                break;
            }

            List<FoundPortalApiItemVO> items =
                    response.getBody()
                            .getItems()
                            .getItem();

            if (items.isEmpty()) {

                if (pageNo == 1) {
                    throw new IllegalStateException(
                        "포털기관 습득물 API 수집 결과가 "
                        + "0건입니다."
                    );
                }

                System.out.println(
                    "포털기관 습득물 item 리스트 비어 있음. "
                    + "수집 종료"
                );

                break;
            }

            String firstAtcId =
                    getAtcId(items.get(0));

            String lastAtcId =
                    getAtcId(
                        items.get(items.size() - 1)
                    );

            String pageKey =
                    items.size()
                    + ":"
                    + firstAtcId
                    + ":"
                    + lastAtcId;

            /*
             * 이전 페이지와 동일한 첫 번째·마지막 atcId가
             * 다시 들어오면 반복 응답으로 판단합니다.
             */
            if (!collectedPageKeys.add(pageKey)) {
                throw new IllegalStateException(
                    "포털기관 습득물 API가 동일한 페이지를 "
                    + "반복 반환했습니다. "
                    + "pageNo = " + pageNo
                    + ", firstAtcId = " + firstAtcId
                    + ", lastAtcId = " + lastAtcId
                );
            }

            int addedCount = 0;

            /*
             * API 수집 단계에서 atcId 중복을 제거합니다.
             */
            for (FoundPortalApiItemVO item : items) {

                if (item == null ||
                    item.getAtcId() == null ||
                    item.getAtcId()
                        .trim()
                        .isEmpty()) {

                    continue;
                }

                String atcId =
                        item.getAtcId().trim();

                if (collectedAtcIds.add(atcId)) {
                    allItems.add(item);
                    addedCount++;
                }
            }

            System.out.println(
                "포털기관 습득물 API 응답 완료"
                + " / pageNo = " + pageNo
                + " / 응답 건수 = " + items.size()
                + " / 신규 건수 = " + addedCount
                + " / 누적 고유 건수 = " + allItems.size()
                + " / 첫 atcId = " + firstAtcId
                + " / 마지막 atcId = " + lastAtcId
                + " / 페이지 소요 시간 = "
                + (pageEndTime - pageStartTime)
                + "ms"
            );

            if (items.size() < NUM_OF_ROWS) {
                System.out.println(
                    "포털기관 습득물 마지막 페이지 도달. "
                    + "수집 종료"
                );

                break;
            }

            /*
             * 1,000건을 받았는데 신규 atcId가 하나도 없다면
             * 같은 데이터가 다른 순서로 반복된 것으로 판단합니다.
             */
            if (addedCount == 0) {
                throw new IllegalStateException(
                    "현재 페이지에서 신규 데이터가 0건입니다. "
                    + "API 반복 응답 가능성이 있습니다. "
                    + "pageNo = " + pageNo
                );
            }

            pageNo++;

            Thread.sleep(300);
        }

        long apiEndTime =
                System.currentTimeMillis();

        if (allItems.isEmpty()) {
            throw new IllegalStateException(
                "수집된 포털기관 습득물 데이터가 0건이므로 "
                + "DB 저장을 실행하지 않습니다."
            );
        }

        System.out.println(
            "========================================"
        );
        System.out.println(
            "포털기관 습득물 API 수집 완료"
        );
        System.out.println(
            "총 페이지 수 = " + pageNo
        );
        System.out.println(
            "총 고유 item 수 = " + allItems.size()
        );
        System.out.println(
            "API 수집 소요 시간 = "
            + (apiEndTime - apiStartTime)
            + "ms"
        );
        System.out.println(
            "DB UPSERT 시작"
        );
        System.out.println(
            "========================================"
        );

        long dbStartTime =
                System.currentTimeMillis();

        int saveCount =
                foundPortalTxService
                    .upsertFoundPortalItems(
                        allItems,
                        today,
                        sixMonthsAgo
                    );

        long dbEndTime =
                System.currentTimeMillis();

        long totalEndTime =
                System.currentTimeMillis();

        System.out.println(
            "========================================"
        );
        System.out.println(
            "최근 6개월 포털기관 습득물 재수집 완료"
        );
        System.out.println(
            "총 API item 수 = " + allItems.size()
        );
        System.out.println(
            "총 저장 건수 = " + saveCount
        );
        System.out.println(
            "API 수집 시간 = "
            + (apiEndTime - apiStartTime)
            + "ms"
        );
        System.out.println(
            "DB 저장 시간 = "
            + (dbEndTime - dbStartTime)
            + "ms"
        );
        System.out.println(
            "전체 실행 시간 = "
            + (totalEndTime - totalStartTime)
            + "ms"
        );
        System.out.println(
            "========================================"
        );
    }

    private String getAtcId(
            FoundPortalApiItemVO item) {

        if (item == null ||
            item.getAtcId() == null) {

            return "null";
        }

        return item.getAtcId().trim();
    }

    private byte[] getBytesWithRetry(
            RestTemplate restTemplate,
            String url,
            int pageNo
    ) throws InterruptedException {

        int maxRetry = 3;

        for (int retry = 1;
             retry <= maxRetry;
             retry++) {

            try {
                return restTemplate.getForObject(
                    url,
                    byte[].class
                );

            } catch (
                HttpServerErrorException.GatewayTimeout e
            ) {

                System.out.println(
                    "504 Gateway Timeout 발생"
                    + " / pageNo = " + pageNo
                    + " / 재시도 = " + retry
                    + "회"
                );

                if (retry == maxRetry) {
                    throw e;
                }

                Thread.sleep(3000L * retry);
            }
        }

        return null;
    }
}