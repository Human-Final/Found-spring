package com.human.found.domain.found.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.human.found.domain.found.mapper.FoundPortalMapper;
import com.human.found.domain.found.vo.FoundPortalApiItemVO;
import com.human.found.domain.found.vo.FoundPortalApiResponseVO;
import com.human.found.domain.found.vo.FoundVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoundPortalService {
    
    private final FoundPortalMapper foundPortalMapper;

    @Value("${portal.api.url}")
    private String apiUrl;

    @Value("${police.api.key}")
    private String serviceKey;


    public void saveFoundPortalData() throws Exception{
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
            .queryParam("serviceKey", serviceKey)
            .queryParam("pageNo", 1)
            .queryParam("numOfRows", 10)
            .build(false)
            .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        byte[] bytes = restTemplate.getForObject(url, byte[].class);
        String xml = new String(bytes, StandardCharsets.UTF_8);

        System.out.println(xml.substring(0, Math.min(xml.length(), 1000)));

        
        XmlMapper xmlMapper = new XmlMapper();
        FoundPortalApiResponseVO response = 
            xmlMapper.readValue(xml, FoundPortalApiResponseVO.class);

        List<FoundPortalApiItemVO> items = 
            response.getBody().getItems().getItem();

        for(FoundPortalApiItemVO item : items){
            if(foundPortalMapper.existsByAtcId(item.getAtcId())>0){
                continue;
            }
            FoundVO foundVO = new FoundVO();
            foundVO.setAtcId(item.getAtcId());
            foundVO.setClrNm(item.getClrNm());
            foundVO.setDepPlace(item.getDepPlace());
            foundVO.setFdFilepathImg(item.getFdFilepathImg());
            foundVO.setFdPrdtNm(item.getFdPrdtNm());
            foundVO.setFdSbjt(item.getFdSbjt());
            foundVO.setFdYmd(LocalDate.parse(item.getFdYmd()).atStartOfDay());
            foundVO.setPrdtClNm(item.getPrdtClNm());

            foundPortalMapper.insertFoundPortal(foundVO);

        } 
    }
}
