package com.human.found.domain.lost.vo;

import lombok.Data;

@Data
public class LostFileVO {
    private String id;
    private String fdFilepathImg;
    private String atcId;
    private String saveName;
    private Long fileSize;
    private String filePath;
}
