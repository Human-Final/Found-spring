package com.human.found.infrastructure.police.foundPolicePortal.vo;

import java.time.LocalDate;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoundPortalApiItemVO {
    private String atcId;
    private String fdSn;
    private String prdtClNm;
    private String clrNm;
    private String fdPrdtNm;
    private String fdSbjt;
    private String depPlace;
    private String fdYmd;
    private String rnum;

    @JacksonXmlProperty(localName="fdFilePathImg")
    private String fdFilepathImg;

}
