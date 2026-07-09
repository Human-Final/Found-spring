package com.human.found.domain.user.vo;

import static com.human.found.global.common.validation.UserValidationRules.*;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserVO {

    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = USER_ID_REGEX, message = USER_ID_MESSAGE)
    private String id;

    private String pw;

    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = USER_NAME_REGEX, message = USER_NAME_MESSAGE)
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Pattern(regexp = USER_EMAIL_REGEX, message = USER_EMAIL_MESSAGE)
    private String email;
    
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = TEL_REGEX, message = TEL_MESSAGE)
    private String tel;

    @NotBlank(message = "권한은 필수입니다.")
    @Pattern(regexp = ROLE_REGEX, message = ROLE_MESSAGE)
    private String role;

    private LocalDateTime signUp;
    private LocalDateTime deletedAt;
    
    @Min(value = 0, message = "삭제 여부 값이 올바르지 않습니다.")
    @Max(value = 1, message = "삭제 여부 값이 올바르지 않습니다.")
    private int isDeleted;

    private String pwCheck;
    
    @NotBlank(message = "회원 상태는 필수입니다.")
    @Pattern(regexp = STATUS_REGEX, message = STATUS_MESSAGE)
    private String status;


}
