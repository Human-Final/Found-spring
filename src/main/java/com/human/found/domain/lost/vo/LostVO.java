package com.human.found.domain.lost.vo;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LostVO {
    private Long num;
    private String atcId;
    private String id;
    private String lstPlace;
    @NotBlank(message = "물품명은 필수 입력 사항입니다.")
    private String lstPrdtNm;
    @NotBlank(message = "게시글 제목은 필수 입력 사항입니다.")
    private String lstSbjt;
    // @NotBlank(message = "분실일자는 필수 입력 사항입니다.")
    private LocalDateTime lstYmd;
    private String prdtClNm;
    private String prdtCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer done = 0;
    private Integer isDeleted = 0;
    private List<LostFileVO> fileList;
    private String dataSource;
    private long viewCount;
    private String lstFilepathImg;
    

}
