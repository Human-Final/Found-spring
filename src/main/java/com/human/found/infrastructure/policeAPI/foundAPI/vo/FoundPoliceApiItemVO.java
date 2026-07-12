package com.human.found.infrastructure.policeAPI.foundAPI.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoundPoliceApiItemVO {

    private String atcId;
    private String clrNm;
    private String depPlace;
    private String fdFilePathImg;
    private String fdPrdtNm;
    private String fdSbjt;
    private String fdYmd;
    private String prdtClNm;
}