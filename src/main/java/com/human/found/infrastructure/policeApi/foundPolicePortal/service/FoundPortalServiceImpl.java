package com.human.found.infrastructure.policeApi.foundPolicePortal.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.human.found.infrastructure.policeApi.foundPolicePortal.vo.FoundPortalApiItemVO;
import com.human.found.infrastructure.policeApi.foundPolicePortal.vo.FoundPortalApiResponseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoundPortalServiceImpl implements FoundPortalService {
    
    private final FoundPortalTxService foundPortalTxService;

    @Value("${portal.api.url}")
    private String apiUrl;

    @Value("${police.api.key}")
    private String serviceKey;

    @Override
    public void saveFoundPortalData() throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        XmlMapper xmlMapper = new XmlMapper();

        int pageNo = 1;
        int numOfRows = 9999;

        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6);

        List<FoundPortalApiItemVO> allItems = new ArrayList<>();

        while (true) {

            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("START_YMD", sixMonthsAgo.format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("END_YMD", today.format(DateTimeFormatter.BASIC_ISO_DATE))
                .build(false)
                .toUriString();
            System.out.println("요청 URL = " + url);
            System.out.println("포털기관 습득물 API 요청 pageNo = " + pageNo);

            byte[] bytes = restTemplate.getForObject(url, byte[].class);

            if (bytes == null || bytes.length == 0) {
                System.out.println("API 응답 없음. 수집 종료");
                break;
            }

            String xml = new String(bytes, StandardCharsets.UTF_8);

            System.out.println(xml.substring(0, Math.min(xml.length(), 1000)));

            FoundPortalApiResponseVO response =
                xmlMapper.readValue(xml, FoundPortalApiResponseVO.class);

            if (response == null ||
                response.getBody() == null ||
                response.getBody().getItems() == null ||
                response.getBody().getItems().getItem() == null) {

                System.out.println("item 없음. 수집 종료");
                break;
            }

            List<FoundPortalApiItemVO> items =
                response.getBody().getItems().getItem();
            if (items.isEmpty()) {
                System.out.println("item 리스트 비어 있음. 수집 종료");
                break;
            }

            allItems.addAll(items);
            
            if (items.size() < numOfRows) {
                System.out.println("마지막 페이지 도달. 수집 종료");
                break;
            }

            pageNo++;

            Thread.sleep(300);
        }

        int insertCount = foundPortalTxService.replaceItems(
            allItems,
            today,
            sixMonthsAgo
        );

        System.out.println("최근 6개월 포털기관 습득물 API 재수집 완료");
        System.out.println("총 API item 수 = " + allItems.size());
        System.out.println("총 저장 건수 = " + insertCount);
    }

}