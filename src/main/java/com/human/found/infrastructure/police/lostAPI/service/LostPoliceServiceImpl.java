package com.human.found.infrastructure.police.lostAPI.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.human.found.domain.lost.vo.LostVO;
import com.human.found.infrastructure.police.lostAPI.mapper.LostPoliceMapper;

@Service
public class LostPoliceServiceImpl implements LostPoliceService{

    @Autowired
    private LostPoliceMapper lostPoliceMapper;
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${api.policelost.url}")
    private String baseUrl;

    @Value("${police.api.key}")
    private String serviceKey;
    //initialDelay = 5000 5초뒤에 경찰청 분실물 API자료 갖고오는 테스트용 코드
    //스케줄러 메서드
    //매일 새벽 1시에 자동으로 아래 fetchAndSaveLostGoods 메서드 호출하기
    @Scheduled(initialDelay = 5000)
    public void ScheduledSavePoliceLost() {
        System.out.println("⏰ [스케줄러 시작] 기존 데이터를 삭제하고 경찰청의 모든 유실물 데이터를 처음부터 끝까지 수집합니다.");
        
        int pageNo = 1;
        int numOfRows = 2000; // API가 허용하는 안전한 최대 한도치로 설정
        
        while (true) {
            System.out.println("🔄 현재 " + pageNo + "페이지 수집 중... (한 번에 " + numOfRows + "개씩 요청)");
            
            // 기존에 만든 수집 메서드를 호출하되, 이번 회차에 수집된 데이터 개수를 반환받도록 설계하는 것이 좋습니다.
            // 아래는 기존 구조를 유지하면서 반복문을 도는 안전한 가이드입니다.
            try {
                // 현재 페이지 데이터를 긁어서 DB에 저장
                savePoliceLost(pageNo, numOfRows);
                
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
    
    // 경찰청 분실물 저장하기
    public void savePoliceLost(int pageNo, int numOfRows) {
        List<LostVO> list = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        
        LocalDate today = LocalDate.now();
        LocalDate sixAgo = today.minusMonths(6);

        String endDate = today.format(formatter);
        String startDate = sixAgo.format(formatter);
        
        LocalDateTime deleteThreshold = sixAgo.atStartOfDay();
        
        try {
            // 주소 및 파라미터 조립 (인증키 분리 결합 구조 유지)
            String policeUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("START_YMD", startDate)
                    .queryParam("END_YMD", endDate)
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .build()
                    .toUriString();

            URI uri = URI.create(policeUrl + "&serviceKey=" + serviceKey);

            // [수정] XML 통신 데이터의 깨짐을 막기 위해 헤더에 application/xml을 명시합니다.
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/xml");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // [수정] 문자열(String.class) 대신 바이트 배열(byte[].class)로 받아야 
            // 눈에 보이지 않는 공백이나 인코딩 부호(BOM)로 인한 프롤로그 파싱 오류를 원천 차단할 수 있습니다.
            ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
            byte[] xmlBytes = response.getBody();

            if (xmlBytes == null || xmlBytes.length == 0) {
                System.out.println("⚠️ 경찰청 API로부터 데이터를 수신하지 못했습니다.");
                return;
            }

            //[디버깅용 정보 확인] 데이터가 잘 가공되어 출력되는지 검증하기 위함입니다.
            String checkXmlText = new String(xmlBytes, "UTF-8");
            System.out.println("🌐 [경찰청 API XML 실제 데이터 확인]:\n" + checkXmlText);

            //[기존 로직 100% 유지] 바이트 배열을 파싱 기기에 그대로 주입합니다.
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlBytes));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("item");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) node;
                    
                    // LostVO 규칙 매핑
                    LostVO vo = new LostVO();
                    
                    vo.setAtcId(getTagValue("atcId", el));
                    vo.setLstSbjt(getTagValue("lstSbjt", el)); 

                    if (getTagValue("lstPlace", el) != null) {
                        vo.setLstPlace(getTagValue("lstPlace", el).replaceAll("\\s*\\(.*?\\)", ""));
                    }

                    vo.setLstPrdtNm(getTagValue("lstPrdtNm", el));

                    if (getTagValue("lstYmd", el) != null && !getTagValue("lstYmd", el).trim().isEmpty()) {
                        try {
                            LocalDate parsedDate = LocalDate.parse(getTagValue("lstYmd", el).trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                            vo.setLstYmd(parsedDate.atStartOfDay()); 
                        } catch (Exception e) {
                            vo.setLstYmd(LocalDateTime.now()); 
                        }
                    }

                    // [수정 핵심: 다단 카테고리 분리 파싱 적용] 
                    String rawPrdtClNm = getTagValue("prdtClNm", el); // 예: "가방 > 남성용 가방" 또는 "악기 > 바이올린"
                    
                    String mainCategory = "기타"; 
                    String subCategory = "기타";  

                    if (rawPrdtClNm != null && !rawPrdtClNm.trim().isEmpty()) {
                        String mainPart = "";
                        String subPart = "";

                        // 1. ">" 구분자 유무에 따라 안전하게 split 처리
                        if (rawPrdtClNm.contains(">")) {
                            String[] parts = rawPrdtClNm.split(">");
                            mainPart = parts[0].trim();
                            subPart = parts[1].trim();
                        } else {
                            mainPart = rawPrdtClNm.trim();
                            subPart = "기타";
                        }

                        // 2. 기존 조건문을 활용한 9대 핵심 대분류 라벨링 규칙
                        if (mainPart.contains("가방") || mainPart.contains("백")) {
                            mainCategory = "가방";
                        } else if (mainPart.contains("귀금속") || mainPart.contains("반지") || mainPart.contains("목걸이") || mainPart.contains("귀걸이") || mainPart.contains("시계")) {
                            mainCategory = "귀금속";
                        } else if (mainPart.contains("도서") || mainPart.contains("책") || mainPart.contains("서적") || mainPart.contains("소설")) {
                            mainCategory = "도서";
                        } else if (mainPart.contains("의류") || mainPart.contains("모자") || mainPart.contains("신발")) {
                            mainCategory = "의류";
                        } else if (mainPart.contains("자동차") || mainPart.contains("네비") || mainPart.contains("번호판")) {
                            mainCategory = "자동차";
                        } else if (mainPart.contains("핸드폰") || mainPart.contains("휴대폰") || mainPart.contains("아이폰")) {
                            mainCategory = "핸드폰"; 
                        } else if (mainPart.contains("전자") || mainPart.contains("노트북") || mainPart.contains("컴퓨터")) {
                            mainCategory = "전자기기";
                        } else if (mainPart.contains("지갑")) {
                            mainCategory = "지갑";
                        } else if (mainPart.contains("카드")) {
                            mainCategory = "카드";
                        }

                        // 3. 서류, 악기 등 9대 항목 외 부가 카테고리는 소분류 유실을 방지하기 위해 상위단 단어를 이식
                        if ("기타".equals(mainCategory)) {
                            subCategory = mainPart; 
                        } else {
                            subCategory = subPart;  
                        }
                    }

                    // 4. 확장된 LostVO에 대분류/소분류 값 각각 셋팅
                    vo.setPrdtClNm(mainCategory);     // 대분류 저장 (ex: 가방, 핸드폰, 기타)
                    vo.setPrdtCategory(subCategory); // 소분류 저장 (ex: 여성용 가방, 악기, 기타)

                    vo.setCreatedAt(LocalDateTime.now());
                    vo.setUpdatedAt(LocalDateTime.now());
                    vo.setDone(0);
                    vo.setIsDeleted(0);

                    list.add(vo); 
                }
            }

            // 대량 데이터 배치 적재 실행
            if (!list.isEmpty()) {
                int insertedCount = lostPoliceMapper.upsertLostPolice(list);
                System.out.println("✅ [DB 적재 성공] " + insertedCount + "건의 경찰청 유실물 데이터를 저장했습니다.");

                int deletedCount = lostPoliceMapper.softDeleteOldLostPolice(deleteThreshold);
                System.out.println("🧹 [소프트 딜리트] 6개월 경과 데이터 " + deletedCount + "건을 삭제 처리했습니다.");
            } else {
                System.out.println("⚠️ 파싱된 유실물 데이터 리스트가 비어있습니다.");
            }

        } catch (Exception e) {
            System.out.println("❌ XML 파싱 또는 DB 처리 중 에러 발생");
            e.printStackTrace();
        }
    }


    //경찰청 분실물 정보 전체 갖고오기
    public List<LostVO> getLostGoodsFromDB() {
        return lostPoliceMapper.selectLostPoliceList();
    }

    public LostVO getDetailByAtcId(String atcId) {
        return lostPoliceMapper.selectLostDetail(atcId);
    }

    private String getTagValue(String tag, Element el) {
        NodeList nl = el.getElementsByTagName(tag);
        return (nl != null && nl.getLength() > 0 && nl.item(0) != null) ? nl.item(0).getTextContent().trim() : "";
    }

}