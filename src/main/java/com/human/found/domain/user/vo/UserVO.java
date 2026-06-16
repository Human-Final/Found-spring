package com.human.found.domain.user.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserVO {
    private String id;
    private String pw;
    private String email;
    private String tel;
    private String role;

    private LocalDateTime signUp;
    private LocalDateTime deletedAt;
    
    private int isDeleted;
}
