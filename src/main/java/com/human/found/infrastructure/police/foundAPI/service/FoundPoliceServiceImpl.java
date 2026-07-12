package com.human.found.infrastructure.police.foundAPI.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.infrastructure.police.foundAPI.mapper.FoundPoliceMapper;
import com.human.found.infrastructure.police.foundAPI.vo.FoundPoliceApiItemVO;
import com.human.found.infrastructure.police.foundAPI.vo.FoundPoliceApiResponseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoundPoliceServiceImpl implements FoundPoliceService {

    private final FoundPoliceMapper foundPoliceMapper;

    @Value("${police.api.url}")
    private String apiUrl;

    @Value("${police.api.key}")
    private String serviceKey;

    /**
     * 경찰청 습득물 API 호출 후 DB 저장
     */
    @Override
    public String savePoliceFoundItems() {

        RestTemplate restTemplate = new RestTemplate();
        XmlMapper xmlMapper = new XmlMapper();

        int pageNo = 1;
        int numOfRows = 100;

        int insertCount = 0;
        int skipCount = 0;
        int failCount = 0;

        String startYmd = "20260709";
        String endYmd = "20260710";

        try {

            while (true) {

                String url = UriComponentsBuilder
                        .fromHttpUrl(apiUrl)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("START_YMD", startYmd)
                        .queryParam("END_YMD", endYmd)
                        .build(false)
                        .toUriString();

                System.out.println(
                        "경찰청 습득물 API 요청 pageNo = " + pageNo
                );

                /*
                 * 한글 깨짐 방지를 위해 byte[]로 받은 뒤 UTF-8 변환
                 */
                byte[] bytes =
                        restTemplate.getForObject(url, byte[].class);

                if (bytes == null || bytes.length == 0) {
                    System.out.println("경찰청 API 응답 없음");
                    break;
                }

                String xml =
                        new String(bytes, StandardCharsets.UTF_8);

                FoundPoliceApiResponseVO response =
                        xmlMapper.readValue(
                                xml,
                                FoundPoliceApiResponseVO.class
                        );

                if (response == null
                        || response.getBody() == null
                        || response.getBody().getItems() == null
                        || response.getBody()
                                   .getItems()
                                   .getItem() == null) {

                    System.out.println("경찰청 습득물 item 없음");
                    break;
                }

                List<FoundPoliceApiItemVO> items =
                        response.getBody()
                                .getItems()
                                .getItem();

                if (items.isEmpty()) {
                    System.out.println("경찰청 습득물 목록 비어 있음");
                    break;
                }

                for (FoundPoliceApiItemVO item : items) {

                    try {

                        String atcId = item.getAtcId();

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
                        vo.setClrNm(item.getClrNm());
                        vo.setDepPlace(item.getDepPlace());
                        vo.setFdFilepathImg(item.getFdFilePathImg());
                        vo.setFdPrdtNm(item.getFdPrdtNm());
                        vo.setFdSbjt(item.getFdSbjt());
                        vo.setFdYmd(parseDate(item.getFdYmd()));
                        vo.setPrdtClNm(
                                getCategoryLabel(item.getPrdtClNm())
                        );
                        vo.setDone(0);
                        vo.setIsDeleted(0);

                        int result =
                                foundPoliceMapper.insertFoundPolice(vo);

                        if (result > 0) {
                            insertCount++;
                        }

                    } catch (Exception e) {
                        failCount++;

                        System.err.println(
                                "개별 저장 실패 atcId = "
                                + item.getAtcId()
                        );
                    }
                }

                int totalCount =
                        response.getBody().getTotalCount();

                if (pageNo * numOfRows >= totalCount
                        || items.size() < numOfRows) {

                    System.out.println(
                            "경찰청 습득물 마지막 페이지 도달"
                    );

                    break;
                }

                pageNo++;
            }

            return insertCount
                    + "건 저장 완료 / "
                    + skipCount
                    + "건 중복 제외 / "
                    + failCount
                    + "건 실패";

        } catch (Exception e) {

            e.printStackTrace();

            return "경찰청 습득물 저장 실패: "
                    + e.getMessage();
        }
    }

    /**
     * 날짜 문자열을 LocalDateTime으로 변환
     */
    private LocalDateTime parseDate(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        LocalDate date;

        if (value.contains("-")) {
            date = LocalDate.parse(value);
        } else {
            date = LocalDate.parse(
                    value,
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );
        }

        return date.atStartOfDay();
    }

    /**
     * 경찰청 분류를 프로젝트 분류로 변경
     */
    private String getCategoryLabel(String value) {

        if (value == null || value.isBlank()) {
            return "기타";
        }

        if (value.contains("가방") || value.contains("백")) {
            return "가방";
        }

        if (value.contains("귀금속")
                || value.contains("반지")
                || value.contains("목걸이")
                || value.contains("귀걸이")
                || value.contains("시계")) {
            return "귀금속";
        }

        if (value.contains("도서")
                || value.contains("책")
                || value.contains("서적")) {
            return "도서";
        }

        if (value.contains("의류")
                || value.contains("옷")
                || value.contains("모자")
                || value.contains("신발")) {
            return "의류";
        }

        if (value.contains("자동차")
                || value.contains("차량")
                || value.contains("번호판")) {
            return "자동차";
        }

        if (value.contains("핸드폰")
                || value.contains("휴대폰")
                || value.contains("스마트폰")
                || value.contains("아이폰")
                || value.contains("갤럭시")) {
            return "핸드폰";
        }

        if (value.contains("전자")
                || value.contains("노트북")
                || value.contains("컴퓨터")
                || value.contains("태블릿")
                || value.contains("이어폰")) {
            return "전자기기";
        }

        if (value.contains("지갑")) {
            return "지갑";
        }

        if (value.contains("카드")) {
            return "카드";
        }

        return "기타";
    }
}