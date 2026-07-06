package com.human.found.domain.user.vo;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserVO {
    private String id;
    private String pw;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    
    @Pattern(
        regexp = "^010[0-9]{8}$",
        message = "전화번호는 010으로 시작하는 숫자 11자리여야 합니다."
    )
    private String tel;

    private String role;

    private LocalDateTime signUp;
    private LocalDateTime deletedAt;
    
    private int isDeleted;

    private String pwCheck;
    
    private String status;


}
