package com.human.found.infrastructure.police.lost;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LostGoodsApi {
    public static void main(String[] args) {
        // 공공데이터포털에서 발급받은 본인의 인증키를 입력하세요 (Decoding 키 권장)
        String serviceKey = "YOUR_SERVICE_KEY_HERE"; 
        
        try {
            // 1. URL 및 요청 파라미터 빌드 (URL 인코딩 필수)
            StringBuilder urlBuilder = new StringBuilder("https://data.go.kr");
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey); // 인증키는 이미 인코딩된 상태인 경우가 많아 원본 그대로 대입
            urlBuilder.append("&" + URLEncoder.encode("START_YMD", "UTF-8") + "=" + URLEncoder.encode("20260101", "UTF-8")); // 조회시작일
            urlBuilder.append("&" + URLEncoder.encode("END_YMD", "UTF-8") + "=" + URLEncoder.encode("20260615", "UTF-8"));   // 조회종료일
            urlBuilder.append("&" + URLEncoder.encode("PRDT_CL_CD", "UTF-8") + "=" + URLEncoder.encode("PRD010000", "UTF-8")); // 분류코드
            urlBuilder.append("&" + URLEncoder.encode("LST_LCT_CD", "UTF-8") + "=" + URLEncoder.encode("LCI010000", "UTF-8")); // 지역코드
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8"));

            // 2. HTTP 커넥션 설정 (데이터 조회이므로 GET 방식 사용)
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/xml");
            
            System.out.println("API 요청 중... (응답 코드: " + conn.getResponseCode() + ")");

            // 3. 응답 데이터 읽기
            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();

            // 4. XML 데이터 파싱 시작
            String xmlData = sb.toString();
            parseXml(xmlData);

        } catch (Exception e) {
            System.err.println("❌ 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // XML 파싱을 처리하는 메서드
    private static void parseXml(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // 문자열을 바이트 스트림으로 변환하여 파싱
            Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
            doc.getDocumentElement().normalize();

            // 에러 체크 (resultCode가 00이 아니면 에러)
            NodeList headerList = doc.getElementsByTagName("header");
            if (headerList.getLength() > 0) {
                Element header = (Element) headerList.item(0);
                String resultCode = getTagValue("resultCode", header);
                String resultMsg = getTagValue("resultMsg", header);
                
                if (!"00".equals(resultCode)) {
                    System.out.println("⚠️ API 에러 발생 - 코드: " + resultCode + ", 메시지: " + resultMsg);
                    System.out.println("응답 본문: " + xmlString);
                    return;
                }
            }

            // <item> 태그들을 전부 가져옴
            NodeList nList = doc.getElementsByTagName("item");
            System.out.println("\n=== 분실물/습득물 조회 결과 (총 " + nList.getLength() + "건) ===");

            if (nList.getLength() == 0) {
                System.out.println("조회된 데이터가 없습니다.");
                return;
            }

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    // 문서 가이드(Response Element)에 정의된 태그 이름과 매칭하여 데이터 추출
                    String atcId = getTagValue("atcId", eElement);       // 관리번호
                    String lstNm = getTagValue("lstNm", eElement);       // 물품명
                    String lstYmd = getTagValue("lstYmd", eElement);     // 분실일자
                    String lstPlace = getTagValue("lstPlace", eElement); // 분실장소

                    System.out.println("■ 관리번호: " + atcId);
                    System.out.println("  물품명  : " + lstNm);
                    System.out.println("  분실일자: " + lstYmd);
                    System.out.println("  분실장소: " + lstPlace);
                    System.out.println("------------------------------");
                }
            }
        } catch (Exception e) {
            System.err.println("XML 파싱 오류: " + e.getMessage());
        }
    }

    // 태그 내의 텍스트 값을 안전하게 가져오기 위한 헬퍼 메서드
    private static String getTagValue(String tag, Element element) {
        NodeList nlList = element.getElementsByTagName(tag);
        if (nlList != null && nlList.getLength() > 0) {
            Node nValue = nlList.item(0);
            if (nValue != null) {
                return nValue.getTextContent();
            }
        }
        return "정보 없음";
    }
}
