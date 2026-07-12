package com.human.found.infrastructure.police.foundAPI.vo;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoundPortalApiResponseVO {
    
    private ApiHeader header;
    private ApiBody body;

    @Getter
    @Setter
    public static class ApiHeader{
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    public static class ApiBody{
        private int numOfRows;
        private int pageNo;
        private int totalCount;
        private ApiItems items;
    }

    @Getter
    @Setter
    public static class ApiItems{
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<FoundPortalApiItemVO> item;
    }
}
