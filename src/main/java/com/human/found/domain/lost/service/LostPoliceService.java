package com.human.found.domain.lost.service;

import com.human.found.domain.lost.mapper.LostPoliceMapper;
import com.human.found.domain.lost.vo.LostPoliceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

@Service
public class LostPoliceService {

    @Autowired
    private LostPoliceMapper lostMapper;

    private final String serviceKey = "51cb7bbc7238b3a05c50974e40c97261a36015bddc473118eae5cc3c273094ce";

    public void fetchAndSaveLostGoods(int pageNo, int numOfRows) {
        List<LostPoliceVO> list = new ArrayList<>();
        
        try {
            String baseUrl = "https://apis.data.go.kr/1320000/LostGoodsInfoInqireService/getLostGoodsInfoAccToClAreaPd";
            StringBuilder sbUrl = new StringBuilder(baseUrl);
            
            sbUrl.append("?serviceKey=").append(serviceKey);
            sbUrl.append("&START_YMD=").append(URLEncoder.encode("20260101", "UTF-8"));
            sbUrl.append("&END_YMD=").append(URLEncoder.encode("20260615", "UTF-8"));
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
                    
                    // 수정할 for 루프 내부 (내용 태그가 없다면 이대로 두십시오)
                LostPoliceVO vo = new LostPoliceVO();
                vo.setAtcId(getTagValue("atcId", el));
                vo.setLstSbjt(getTagValue("lstSbjt", el)); // 제목

                // 주소 정제 로직은 그대로 유지
                String rawPlace = getTagValue("lstPlace", el);
                String cleanedPlace = rawPlace.replaceAll("\\s*\\(.*?\\)", "");
                vo.setLstPlace(cleanedPlace);

                vo.setLstPrdtNum(getTagValue("lstPrdtNm", el));
                vo.setLstYmd(getTagValue("lstYmd", el));
                vo.setPrdtClNum(getTagValue("prdtClNm", el));

                list.add(vo); // 중복된 setLstSbjt 삭제 완료
                }
            }

            if (!list.isEmpty()) {
                lostMapper.insertLostGoodsBatch(list);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LostPoliceVO> getLostGoodsFromDB() {
        return lostMapper.selectLostGoodsList();
    }

    public LostPoliceVO getDetailByAtcId(String atcId) {
        return lostMapper.selectLostDetail(atcId);
    }

    private String getTagValue(String tag, Element el) {
        NodeList nl = el.getElementsByTagName(tag);
        return (nl != null && nl.getLength() > 0 && nl.item(0) != null) ? nl.item(0).getTextContent().trim() : "";
    }
}