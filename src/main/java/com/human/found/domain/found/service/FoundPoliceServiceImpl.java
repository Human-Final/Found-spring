package com.human.found.domain.found.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.human.found.domain.found.mapper.FoundPoliceMapper;
import com.human.found.domain.found.vo.FoundVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FoundPoliceServiceImpl implements FoundPoliceService {

    private final FoundPoliceMapper foundPoliceMapper;

    @Value("${police.api.key}")
    private String serviceKey;

    @Value("${police.api.url}")
    private String apiUrl;

    @Override
    public String savePoliceFoundItems() {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        int pageNo = 1;
        int numOfRows = 1000;

        int insertCount = 0;
        int skipCount = 0;

        String startYmd = "20260614";
        String endYmd = "20260617";

        try {
            while (true) {

                String url = apiUrl
                        + "?serviceKey=" + serviceKey
                        + "&pageNo=" + pageNo
                        + "&numOfRows=" + numOfRows
                        + "&START_YMD=" + startYmd
                        + "&END_YMD=" + endYmd;

                System.out.println("호출 URL = " + url);

                String json = restTemplate.getForObject(url, String.class);

                System.out.println("API 응답 = " + json);

                JsonNode root = objectMapper.readTree(json);

                JsonNode items = root.path("body").path("items");

                System.out.println("pageNo = " + pageNo);
                System.out.println("items = " + items);
                System.out.println("items size = " + items.size());

                if (items.isMissingNode() || items.isNull() || items.isEmpty()) {
                    break;
                }

                if (items.isObject()) {
                    items = objectMapper.createArrayNode().add(items);
                }

                for (JsonNode item : items) {

                    String atcId = item.path("atcId").asText(null);

                    if (atcId == null || atcId.isBlank()) {
                        skipCount++;
                        continue;
                    }

                    if (foundPoliceMapper.existsByAtcId(atcId) > 0) {
                        skipCount++;
                        continue;
                    }

                    FoundVO vo = new FoundVO();

                    vo.setAtcId(atcId);
                    vo.setId(null);

                    vo.setClrNm(item.path("clrNm").asText(null));
                    vo.setDepPlace(item.path("depPlace").asText(null));
                    vo.setFdFilepathImg(item.path("fdFilePathImg").asText(null));
                    vo.setFdPrdtNm(item.path("fdPrdtNm").asText(null));
                    vo.setFdSbjt(item.path("fdSbjt").asText(null));
                    String originalPrdtClNm = item.path("prdtClNm").asText(null);
                    String categoryLabel = getCategoryLabel(originalPrdtClNm);
                    vo.setPrdtClNm(categoryLabel);

                    String fdYmdStr = item.path("fdYmd").asText(null);

                    if (fdYmdStr != null && !fdYmdStr.isBlank()) {

                        LocalDate fdDate;

                        if (fdYmdStr.contains("-")) {
                            fdDate = LocalDate.parse(fdYmdStr);
                        } else {
                            fdDate = LocalDate.parse(
                                    fdYmdStr,
                                    DateTimeFormatter.ofPattern("yyyyMMdd")
                            );
                        }

                        LocalDateTime fdYmd = fdDate.atStartOfDay();
                        vo.setFdYmd(fdYmd);
                    }

                    vo.setDone(0);
                    vo.setIsDeleted(0);

                    int result = foundPoliceMapper.insertFoundPolice(vo);

                    if (result > 0) {
                        insertCount++;
                    }
                }

                pageNo++;
            }

            return insertCount + "건 저장 완료 / " + skipCount + "건 중복 제외";

        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }

    // 카테고리 매핑 규칙 메서드
    private String getCategoryLabel(String prdtClNm) {

        if (prdtClNm == null || prdtClNm.trim().isEmpty()) {
            return "기타";
        }

        if (prdtClNm.contains("가방") || prdtClNm.contains("백")) {
            return "가방";
        }

        if (prdtClNm.contains("귀금속") || prdtClNm.contains("반지")
                || prdtClNm.contains("목걸이") || prdtClNm.contains("귀걸이")
                || prdtClNm.contains("시계")) {
            return "귀금속";
        }

        if (prdtClNm.contains("도서") || prdtClNm.contains("책")
                || prdtClNm.contains("서적") || prdtClNm.contains("소설")) {
            return "도서";
        }

        if (prdtClNm.contains("의류") || prdtClNm.contains("모자")
                || prdtClNm.contains("신발")) {
            return "의류";
        }

        if (prdtClNm.contains("자동차") || prdtClNm.contains("네비")
                || prdtClNm.contains("번호판")) {
            return "자동차";
        }

        if (prdtClNm.contains("전자") || prdtClNm.contains("핸드폰")
                || prdtClNm.contains("휴대폰") || prdtClNm.contains("아이폰")
                || prdtClNm.contains("노트북") || prdtClNm.contains("컴퓨터")) {
            return "전자기기";
        }

        if (prdtClNm.contains("지갑")) {
            return "지갑";
        }

        if (prdtClNm.contains("카드")) {
            return "카드";
        }

        return "기타";
    }

}