package com.human.found.domain.lost.service;

import com.human.found.domain.lost.mapper.LostPoliceMapper;
import com.human.found.domain.lost.vo.LostPoliceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class LostPoliceService {

    @Autowired
    private LostPoliceMapper lostPoliceMapper;

    // 카테고리 매핑 규칙 메서드
    private String getCategoryLabel(String prdtClNm) {
        if (prdtClNm == null || prdtClNm.trim().isEmpty()) {
            return "기타";
        }

        // [가방] "가방", "백" 포함 여부
        if (prdtClNm.contains("가방") || prdtClNm.contains("백")) {
            return "가방";
        }
        // [귀금속] "귀금속", "반지", "목걸이", "귀걸이", "시계" 포함 여부
        if (prdtClNm.contains("귀금속") || prdtClNm.contains("반지") || 
            prdtClNm.contains("목걸이") || prdtClNm.contains("귀걸이") || prdtClNm.contains("시계")) {
            return "귀금속";
        }
        // [도서] "도서", "책", "서적", "소설" 포함 여부
        if (prdtClNm.contains("도서") || prdtClNm.contains("책") || 
            prdtClNm.contains("서적") || prdtClNm.contains("소설")) {
            return "도서";
        }
        // [의류] "의류", "모자", "신발" 포함 여부
        if (prdtClNm.contains("의류") || prdtClNm.contains("모자") || prdtClNm.contains("신발")) {
            return "의류";
        }
        // [자동차] "자동차", "네비", "번호판" 포함 여부
        if (prdtClNm.contains("자동차") || prdtClNm.contains("네비") || prdtClNm.contains("번호판")) {
            return "자동차";
        }
        // [전자기기] "전자", "핸드폰", "휴대폰", "아이폰", "노트북", "컴퓨터" 포함 여부
        if (prdtClNm.contains("전자") || prdtClNm.contains("핸드폰") || prdtClNm.contains("휴대폰") || 
            prdtClNm.contains("아이폰") || prdtClNm.contains("노트북") || prdtClNm.contains("컴퓨터")) {
            return "전자기기";
        }
        // [지갑]
        if (prdtClNm.contains("지갑")) {
            return "지갑";
        }
        // [카드]
        if (prdtClNm.contains("카드")) {
            return "카드";
        }

        // 조건에 맞지 않는 "증명서" 등의 데이터는 모두 "기타"로 분류됨
        return "기타";
    }


    private final String serviceKey = "51cb7bbc7238b3a05c50974e40c97261a36015bddc473118eae5cc3c273094ce";
    
    //스케줄러 메서드
    //매일 새벽 1시에 자동으로 아래 fetchAndSaveLostGoods 메서드 호출하기
    @Scheduled(cron = "0 12 12 * * *")
    public void runDailyLostGoodsFetch() {
        System.out.println("⏰ [스케줄러 시작] 기존 데이터를 삭제하고 경찰청의 모든 유실물 데이터를 처음부터 끝까지 수집합니다.");
        
        // 1. 먼저 기존 테이블 데이터를 깨끗하게 비웁니다.
        lostPoliceMapper.lostPoliceDelete();        
        
        int pageNo = 1;
        int numOfRows = 1000; // API가 허용하는 안전한 최대 한도치로 설정
        
        while (true) {
            System.out.println("🔄 현재 " + pageNo + "페이지 수집 중... (한 번에 " + numOfRows + "개씩 요청)");
            
            // 기존에 만든 수집 메서드를 호출하되, 이번 회차에 수집된 데이터 개수를 반환받도록 설계하는 것이 좋습니다.
            // 아래는 기존 구조를 유지하면서 반복문을 도는 안전한 가이드입니다.
            try {
                // 현재 페이지 데이터를 긁어서 DB에 저장
                fetchAndSaveLostGoods(pageNo, numOfRows);
                
                // [팁] 만약 이번에 가져온 데이터가 요청한 1000개보다 적거나 없다면, 
                // 더 이상 다음 페이지에 데이터가 없다는 뜻이므로 반복문을 종료(break)합니다.
                // 일단은 대략 안전하게 20페이지(2만 건) 정도까지만 긁거나 조건문을 주어 끊어냅니다.
                
                pageNo++; // 다음 페이지 준비
                
                // 안전장치: 너무 무한루프가 돌지 않도록 최대 30페이지(3만 건)까지만 수집하도록 제한
                if (pageNo > 1) {
                    break;
                }
                
                // API 서버에 과부하를 주지 않기 위해 요청 사이에 0.5초(500ms)씩 휴식 시간을 줍니다.
                Thread.sleep(500); 
                
            } catch (Exception e) {
                System.out.println("❌ 데이터 수집 중 오류 발생으로 수집을 중단합니다.");
                e.printStackTrace();
                break;
            }
        }
        
        System.out.println("✅ [스케줄러 완료] 모든 페이지의 유실물 데이터 수집이 끝났습니다!");
    }

    public void fetchAndSaveLostGoods(int pageNo, int numOfRows) {
        List<LostPoliceVO> list = new ArrayList<>();
        //날짜 포매터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        //오늘 날짜와 오늘로부터 6개월 전 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate sixAgo = today.minusMonths(6);

        //문자열로 포맷시키기
        String endDate=today.format(formatter);
        String startDate=sixAgo.format(formatter);
        
        try {
            String baseUrl = "https://apis.data.go.kr/1320000/LostGoodsInfoInqireService/getLostGoodsInfoAccToClAreaPd";
            StringBuilder sbUrl = new StringBuilder(baseUrl);
            
            sbUrl.append("?serviceKey=").append(serviceKey);
            sbUrl.append("&START_YMD=").append(URLEncoder.encode(startDate, "UTF-8"));
            sbUrl.append("&END_YMD=").append(URLEncoder.encode(endDate, "UTF-8"));
            sbUrl.append("&pageNo=").append(URLEncoder.encode(String.valueOf(pageNo), "UTF-8"));
            sbUrl.append("&numOfRows=").append(URLEncoder.encode(String.valueOf(numOfRows), "UTF-8"));

            URL url = new URL(sbUrl.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            int responseCode = conn.getResponseCode();
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode <= 300 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"
            ));
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
            conn.disconnect();

            String rawXml = response.toString();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(rawXml.getBytes("UTF-8")));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("item");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) node;
                    
                    LostPoliceVO vo = new LostPoliceVO();
                    vo.setAtcId(getTagValue("atcId", el));
                    vo.setLstSbjt(getTagValue("lstSbjt", el)); 

                    String rawPlace = getTagValue("lstPlace", el);
                    String cleanedPlace = rawPlace.replaceAll("\\s*\\(.*?\\)", "");
                    vo.setLstPlace(cleanedPlace);

                    vo.setLstPrdtNm(getTagValue("lstPrdtNm", el));
                    vo.setLstYmd(getTagValue("lstYmd", el));

                    // 🌟 [수정] 자체 카테고리 매핑 함수를 통해 정제된 값 세팅
                    String rawPrdtClNm = getTagValue("prdtClNm", el); // 원본 카테고리명 추출
                    String customCategory = getCategoryLabel(rawPrdtClNm); // 매핑 필터 작동
                    
                    vo.setPrdtClNm(customCategory); // 우리 기준 카테고리(예: "전자기기", "기타") 주입

                    list.add(vo); 
                }
            }// 👈 for 루프가 여기서 끝납니다.

            if (!list.isEmpty()) {
                lostPoliceMapper.insertLostGoodsBatch(list);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LostPoliceVO> getLostGoodsFromDB() {
        return lostPoliceMapper.selectLostGoodsList();
    }

    public LostPoliceVO getDetailByAtcId(String atcId) {
        return lostPoliceMapper.selectLostDetail(atcId);
    }

    private String getTagValue(String tag, Element el) {
        NodeList nl = el.getElementsByTagName(tag);
        return (nl != null && nl.getLength() > 0 && nl.item(0) != null) ? nl.item(0).getTextContent().trim() : "";
    }
}