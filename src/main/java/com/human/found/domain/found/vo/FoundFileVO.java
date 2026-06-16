package com.human.found.domain.found.vo;

import lombok.Data;

@Data
public class FoundFileVO {
    private String id;
    private String fdFilepathImg;
    private Long boardId;
    private String saveName;
    private Long fileSize;
    private String filePath; 
}
