package com.human.found.global.common.validation;

// final을 쓰는 이유 : 상속 X = 필요한 것만 골라 가져가기
public final class UserValidationRules {
    
    // 이 클래스는 객체로 만들지 말라는 의미 -> 값만 모아두는 용도라 객체를 만들 필요 없음
    private UserValidationRules() {}

    public static final String USER_ID_REGEX = "^[a-zA-Z0-9_]{4,20}$";
    public static final String USER_ID_MESSAGE = 
            "아이디는 영문/숫자/_(언더바)만 사용하여 4~20자로 입력해야 합니다.";

    public static final String USER_NAME_REGEX = "^[가-힣a-zA-Z]{2,20}$";
    public static final String USER_NAME_MESSAGE = 
            "이름은 한글 또는 영문 2~20자로 입력해야 합니다.";

    public static final String USER_EMAIL_REGEX = 
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|kr|net)$";
    public static final String USER_EMAIL_MESSAGE = 
            "이메일은 영문/숫자 형식이어야 하며 .com/.kr/.net으로 끝나야 합니다.";

    public static final String TEL_REGEX = "^010[0-9]{8}$";
    public static final String TEL_MESSAGE = "전화번호는 010으로 시작하는 숫자 11자리로 입력해야 합니다.";

    // 권한/회원의 검증이 필요한 이유 : guest, super_admin 같은 이상한 값은 입력되면 안됨
    public static final String ROLE_REGEX = "^(USER|MANAGER|ADMIN)$";
    public static final String ROLE_MESSAGE = "허용되지 않은 권한입니다.";

    public static final String STATUS_REGEX = "^(active|dormant|blocked)$";
    public static final String STATUS_MESSAGE = "허용되지 않은 회원 상태입니다.";

    public static final int PASSWORD_MIN_SIZE = 8;
    public static final int PASSWORD_MAX_SIZE = 20;
    public static final String PASSWORD_SIZE_MESSAGE = 
            "비밀번호는 8~20자로 입력해야 합니다.";
    public static final String PASSWORD_COMPLEXITY_MESSAGE =
            "비밀번호는 대문자, 소문자, 숫자, 특수문자 중 3가지 이상 포함해야 합니다.";


    public static boolean isValidPasswordComplexity(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }

        int count = 0;

        if (password.matches(".*[A-Z].*")) count++;
        if (password.matches(".*[a-z].*")) count++;
        if (password.matches(".*[0-9].*")) count++;
        if (password.matches(".*[^a-zA-Z0-9].*")) count++;

        return count >= 3;
    }
}
