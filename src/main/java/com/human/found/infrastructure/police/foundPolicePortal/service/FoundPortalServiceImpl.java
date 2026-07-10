package com.human.found.infrastructure.police.foundPolicePortal.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.human.found.infrastructure.police.foundPolicePortal.vo.FoundPortalApiItemVO;
import com.human.found.infrastructure.police.foundPolicePortal.vo.FoundPortalApiResponseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoundPortalServiceImpl implements FoundPortalService {
    
    private final FoundPortalTxService foundPortalTxService;

    @Value("${portal.api.url}")
    private String apiUrl;

    @Value("${police.api.url}")
    private String policeApiUrl;

    @Value("${police.api.key}")
    private String serviceKey;

    @Override
    public void saveFoundPortalData() throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        XmlMapper xmlMapper = new XmlMapper();

        int pageNo = 1;
        int numOfRows = 1000;

        LocalDate today = LocalDate.now();
        LocalDate targetDate = LocalDate.now().minusDays(1);

        LocalDate startDate = targetDate;
        LocalDate endDate = targetDate;
        // LocalDate sixMonthsAgo = tgoday.minusMonths(1);

        List<FoundPortalApiItemVO> allItems = new ArrayList<>();

        while (true) {

            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                // .queryParam("START_YMD", sixMonthsAgo.format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("START_YMD", startDate.format(DateTimeFormatter.BASIC_ISO_DATE))
                // .queryParam("END_YMD", today.format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("END_YMD", endDate.format(DateTimeFormatter.BASIC_ISO_DATE))
                .build(false)
                .toUriString();

            System.out.println("포털기관 습득물 API 요청 pageNo = " + pageNo);

            byte[] bytes = getBytesWithRetry(restTemplate, url, pageNo);

            if (bytes == null || bytes.length == 0) {
                System.out.println("포털기관 습득물 API 응답 없음. 수집 종료");
                break;
            }

            String xml = new String(bytes, StandardCharsets.UTF_8);

            // System.out.println(xml.substring(0, Math.min(xml.length(), 1000)));

            FoundPortalApiResponseVO response =
                xmlMapper.readValue(xml, FoundPortalApiResponseVO.class);

            if (response == null ||
                response.getBody() == null ||
                response.getBody().getItems() == null ||
                response.getBody().getItems().getItem() == null) {

                System.out.println("포털기관 습득물 item 없음. 수집 종료");
                break;
            }

            List<FoundPortalApiItemVO> items =
                response.getBody().getItems().getItem();

            if (items.isEmpty()) {
                System.out.println("포털기관 습득물 item 리스트 비어 있음. 수집 종료");
                break;
            }

            allItems.addAll(items);
            
            if (items.size() < numOfRows) {
                System.out.println("포털기관 습득물 마지막 페이지 도달. 수집 종료");
                break;
            }

            pageNo++;

            Thread.sleep(300);
        }

        int saveCount = foundPortalTxService.upsertFoundPortalItems(
                allItems,
                today,
                // sixMonthsAgo
                startDate
        );

        System.out.println("최근 6개월 포털기관 습득물 API 재수집 완료");
        System.out.println("총 API item 수 = " + allItems.size());
        System.out.println("총 저장 건수 = " + saveCount);
    }

    @Override
    public void saveFoundPoliceDataByPortalLogic() throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        XmlMapper xmlMapper = new XmlMapper();

        int pageNo = 1;
        int numOfRows = 9999;

        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(1);

        List<FoundPortalApiItemVO> allItems = new ArrayList<>();

        while (true) {

            String url = UriComponentsBuilder.fromHttpUrl(policeApiUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("START_YMD", sixMonthsAgo.format(DateTimeFormatter.BASIC_ISO_DATE))
                    .queryParam("END_YMD", today.format(DateTimeFormatter.BASIC_ISO_DATE))
                    .build(false)
                    .toUriString();

            System.out.println("경찰청 습득물 API 요청 URL = " + url);
            System.out.println("경찰청 습득물 API 요청 pageNo = " + pageNo);

            byte[] bytes = restTemplate.getForObject(url, byte[].class);

            if (bytes == null || bytes.length == 0) {
                System.out.println("경찰청 습득물 API 응답 없음. 수집 종료");
                break;
            }

            FoundPortalApiResponseVO response =
                    xmlMapper.readValue(bytes, FoundPortalApiResponseVO.class);

            if (response == null
                    || response.getBody() == null
                    || response.getBody().getItems() == null
                    || response.getBody().getItems().getItem() == null) {

                System.out.println("경찰청 습득물 item 없음. 수집 종료");
                break;
            }

            List<FoundPortalApiItemVO> items =
                    response.getBody().getItems().getItem();

            if (items.isEmpty()) {
                System.out.println("경찰청 습득물 item 리스트 비어 있음. 수집 종료");
                break;
            }

            allItems.addAll(items);

            if (items.size() < numOfRows) {
                System.out.println("경찰청 습득물 마지막 페이지 도달. 수집 종료");
                break;
            }

            pageNo++;

            Thread.sleep(300);
        }

        int insertCount = foundPortalTxService.replaceFoundPoliceItems(
                allItems,
                today,
                sixMonthsAgo
        );

        System.out.println("최근 6개월 경찰청 습득물 API 수집 완료");
        System.out.println("총 API item 수 = " + allItems.size());
        System.out.println("총 found_police 저장 건수 = " + insertCount);
    }

    private byte[] getBytesWithRetry(
        RestTemplate restTemplate,
        String url,
        int pageNo
    ) throws InterruptedException {

        int maxRetry = 3;

        for (int retry = 1; retry <= maxRetry; retry++) {
            try {
                return restTemplate.getForObject(url, byte[].class);

            } catch (HttpServerErrorException.GatewayTimeout e) {
                System.out.println("504 Gateway Timeout 발생 pageNo = " + pageNo
                        + " / 재시도 " + retry + "회");

                if (retry == maxRetry) {
                    throw e;
                }

                Thread.sleep(3000L * retry);
            }
        }

        return null;
    }

}