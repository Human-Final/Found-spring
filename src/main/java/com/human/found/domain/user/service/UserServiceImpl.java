package com.human.found.domain.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.comment.vo.CommentVO;
import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.MyPagePostVO;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 회원 관련 서비스 구현체
 * - UserService 인터페이스의 실제 로직을 구현
 * - Controller → Service → Mapper 구조에서 Service 역할 수행
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    // 회원의 아이디 조회
    @Override
    public String findUserId(String name, String email) {
        String userId=userMapper.findUserId(name, email);
        if(userId==null||userId.isEmpty()){
            return null;
        }
        return userId;
    }

    // 회원정보 조회
    @Override
    public boolean isUserExist(String id, String name, String email){
        if(userMapper.isUserExist(id, name, email)>0){
            return true;
        }
        return false;
    }

    // 회원의 비밀번호 조회용 이메일 인증
    @Override
    public String sendPwEmail(String email, HttpSession session) {
        java.util.Random random = new java.util.Random();
        String verificationCode = String.format("%06d", random.nextInt(1000000));
        long expiresAt = System.currentTimeMillis() + (3 * 60 * 1000);

        session.setAttribute("pwEmailAuthCode", verificationCode);
        session.setAttribute("pwEmailAuthTarget", email);
        session.setAttribute("pwEmailAuthExpires", expiresAt);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[FOUND-AI기반 내 물건 찾기 서비스] 비밀번호 재설정 인증번호입니다."); 
            message.setText("안녕하세요. 비밀번호 재설정을 위한 인증번호입니다.\n"
                    + "요청하신 인증번호 6자리는 [" + verificationCode + "] 입니다.\n"
                    + "3분 이내에 화면에 입력해 주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); 
            return "mail_error";
        }
        return "send_success";
    }

    // 비밀번호 찾기 이메일 인증코드 처리
    @Override
    public String verifyPwCode(String inputCode, String email, HttpSession session) {
        String sessionCode = (String) session.getAttribute("pwEmailAuthCode");
        String sessionEmail = (String) session.getAttribute("pwEmailAuthTarget");
        Long expiresAt = (Long) session.getAttribute("pwEmailAuthExpires");

        if (expiresAt == null || sessionCode == null || !email.equals(sessionEmail)) return "no_request";
        if (System.currentTimeMillis() > expiresAt) { return "timeout"; }
        if (!sessionCode.equals(inputCode)) return "wrong_code";

        session.setAttribute("pwVerifiedStatus", true);
        return "verified";
    }

    


    /**
     * 회원가입
     * - 비밀번호 암호화
     * - 회원 정보 저장
     */
    @Override
    public void join(UserVO user) {

        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(user.getPw());
        user.setPw(encodedPw);

        // 회원 저장
        userMapper.insertUser(user);
    }

    /**
     * 아이디로 회원 조회
     * 로그인 시 Security에서 사용
     */
    @Override
    public UserVO findById(String id) {
        return userMapper.findById(id);
    }

    /**
     * 아이디 중복 확인
     * true : 중복
     * false : 사용 가능
     */
    @Override
    public boolean isDuplicatedId(String id) {
        return userMapper.countById(id) > 0;
    }

    /**
     * 이메일 중복 확인
     * true : 중복
     * false : 사용 가능
     */
    @Override
    public boolean isDuplicatedEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }

    // 회원가입 사용자 이메일 확인하기
    @Override
    public String sendJoinEmail(String email, HttpSession session) {
        java.util.Random random = new java.util.Random();
        String verificationCode = String.format("%06d", random.nextInt(1000000));
        long expiresAt = System.currentTimeMillis() + (3 * 60 * 1000);

        session.setAttribute("joinEmailAuthCode", verificationCode);
        session.setAttribute("joinEmailAuthTarget", email);
        session.setAttribute("joinEmailAuthExpires", expiresAt);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[FOUND-AI기반 내 물건 찾기 서비스] 회원가입 이메일 인증번호입니다."); 
            message.setText("안녕하세요. 회원가입을 환영합니다.\n"
                    + "요청하신 가입 인증번호 6자리는 [" + verificationCode + "] 입니다.\n"
                    + "3분 이내에 화면에 입력해 주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); 
            return "mail_error";
        }
        return "send_success";
    }

    // 회원가입 이메일 인증코드 알맞은지 확인하기
    @Override
    public String verifyJoinCode(String inputCode, String email, HttpSession session) {
        String sessionCode = (String) session.getAttribute("joinEmailAuthCode");
        String sessionEmail = (String) session.getAttribute("joinEmailAuthTarget");
        Long expiresAt = (Long) session.getAttribute("joinEmailAuthExpires");

        if (expiresAt == null || sessionCode == null || !email.equals(sessionEmail)) return "no_request";
        if (System.currentTimeMillis() > expiresAt) { session.invalidate(); return "timeout"; }
        if (!sessionCode.equals(inputCode)) return "wrong_code";

        return "verified";
    }

    /**
     * 회원가입 입력값 검증
     */
    @Override
    public String validateJoin(UserVO user) {

        // 아이디 형식 검사
        // 영문과 숫자만 허용, 4~20자
        if (user.getId() == null || !user.getId().matches("^[a-zA-Z0-9]{4,20}$")) {
            return "아이디는 영문과 숫자만 사용 가능하며 4~20자로 입력해주세요.";
        }

        // 아이디 중복
        if (isDuplicatedId(user.getId())) {
            return "이미 사용중인 아이디입니다.";
        }

        // 이메일 중복
        if (isDuplicatedEmail(user.getEmail())) {
            return "이미 사용중인 이메일입니다.";
        }

        if (!user.getPw().equals(user.getPwCheck())) {
            return "비밀번호가 일치하지 않습니다.";
        }

        // 비밀번호 길이
        if (user.getPw().length() < 8) {
            return "비밀번호는 8자 이상이어야 합니다.";
        }

        // 비밀번호 조합 검사
        // 대문자, 소문자, 숫자, 특수문자 중 3가지 이상 포함
        int count = 0;

        if (user.getPw().matches(".*[A-Z].*")) count++;
        if (user.getPw().matches(".*[a-z].*")) count++;
        if (user.getPw().matches(".*[0-9].*")) count++;
        if (user.getPw().matches(".*[^a-zA-Z0-9].*")) count++;

        if (count < 3) {
            return "대문자, 소문자, 숫자, 특수문자 중 3가지 이상 포함해야 합니다.";
        }

        // 검증 통과
        return null;
    }

    @Override
    public java.util.Map<String, Object> getMyPageCounts(String userId) {
        return userMapper.selectMyPageCounts(userId);
    }
    
    /**
     * 현재 비밀번호 본인 인증 판정 로직
     */
    @Override
    public boolean checkPassword(String id, String pwCheck) {

        // 1. DB에서 아이디로 해당 회원 정보를 단건 조회합니다.
        UserVO user = userMapper.findById(id);

        if (user == null || user.getPw() == null) {
            System.out.println("[인증 실패] 존재하지 않는 유저이거나 DB 비밀번호가 null입니다.");
            return false;
        }

        // 2. 브라우저와 DB 통신 과정에서 생길 수 있는 공백을 제거합니다.
        String cleanPwCheck = (pwCheck != null) ? pwCheck.trim() : "";
        String cleanDbPassword = user.getPw().trim();

        // 3. 스프링 시큐리티 인코더 객체를 사용하여 비밀번호를 대조합니다.
        // matches(평문, 암호문) 순서 엄격 준수
        boolean isMatch = passwordEncoder.matches(cleanPwCheck, cleanDbPassword);

        // 콘솔 디버깅을 통해 데이터 무결성을 검증합니다.
        System.out.println("=========================================");
        System.out.println("[마이페이지 본인 인증 디버깅 데이터]");
        System.out.println("조회 대상 아이디 : " + id);
        System.out.println("전달된 입력 평문 : [" + cleanPwCheck + "] (길이: " + cleanPwCheck.length() + ")");
        System.out.println("가져온 DB 암호문 : [" + cleanDbPassword + "]");
        System.out.println("최종 매칭 일치 여부 : " + isMatch);
        System.out.println("=========================================");

        return isMatch;
    }

    /**
     * 마이페이지 메인 화면용 최근 작성글 2개 조회 구현부
     */
    @Override
    @Transactional(readOnly = true) // 단순 조회용 트랜잭션 최적화 보장
    public List<MyPagePostVO> getMyRecentPostList(String userId) {
        
        // 유저 아이디 검증 (null 혹은 공백 방어벽 세우기)
        if (userId == null || userId.trim().isEmpty()) {
            return new java.util.ArrayList<>(); // 안전하게 빈 리스트 반환하여 500에러 차단
        }
        
        // 매퍼 인터페이스를 호출하여 마리아DB 연산 데이터(UNION ALL 결과) 반환
        return userMapper.selectMyRecentPostList(userId);
    }

    /**
     * 마이페이지 메인 화면용 작성글 모두 조회하기 구현부
     */
    @Override
    @Transactional(readOnly = true)
    public List<MyPagePostVO> getMyAllPostList(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return userMapper.selectMyAllPostList(userId);
    }



    /**
     * 🔑 2. 새 비밀번호 유효성 검사 및 변경 처리
     */
    @Override
    @Transactional
    public void updateUserPassword(String id, String newPw) {

        // 서비스단 진입 확인용
        System.out.println("[서비스단] updateUserPassword 비즈니스 로직 가동 시작");
        System.out.println("[서비스단] 입력 파라미터 글자수: " + (newPw != null ? newPw.length() : "null"));

        // 기존 비밀번호 조합 검사 로직 적용
        if (newPw == null || newPw.trim().isEmpty()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }

        if (newPw.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        int count = 0;

        if (newPw.matches(".*[A-Z].*")) count++;
        if (newPw.matches(".*[a-z].*")) count++;
        if (newPw.matches(".*[0-9].*")) count++;
        if (newPw.matches(".*[^a-zA-Z0-9].*")) count++;

        if (count < 3) {
            throw new IllegalArgumentException("대문자, 소문자, 숫자, 특수문자 중 3가지 이상 포함해야 합니다.");
        }

        // 검증 통과 시 암호화하여 DB 업데이트 진행
        String encryptedPw = passwordEncoder.encode(newPw);
        userMapper.updatePasswordOnly(id, encryptedPw);
    }

    /**
     * 회원 정보 수정
     * - 현재 비밀번호 확인
     * - 새 비밀번호 입력 시 암호화
     * - 새 비밀번호 미입력 시 기존 비밀번호 유지
     */
    @Override
    @Transactional
    public void updateUserInfo(UserVO userVO) {

        // 1. DB에서 현재 로그인한 유저의 원본 암호 정보 가져오기
        UserVO dbUser = userMapper.findById(userVO.getId());

        // 2. 입력한 현재 비밀번호와 DB에 암호화되어 저장된 비밀번호가 일치하는지 검증
        // passwordEncoder.matches(평문비밀번호, 암호화된비밀번호)
        if (!passwordEncoder.matches(userVO.getPwCheck(), dbUser.getPw())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호를 입력한 경우에만 암호화해서 세팅, 입력 안 했으면 기존 암호 유지
        if (userVO.getPw() != null && !userVO.getPw().trim().isEmpty()) {
            String encodedNewPw = passwordEncoder.encode(userVO.getPw());
            userVO.setPw(encodedNewPw);
        } else {
            userVO.setPw(dbUser.getPw());
        }

        // 4. DB 업데이트 실행
        userMapper.updateUser(userVO);
    }

    /**
     * 마이페이지 뷰를 띄울 때 회원 정보를 안전하게 꺼내오기 위한 메서드
     */
    @Override
    public UserVO getUserInfo(String id) {
        return userMapper.findById(id);
    }

    @Override
    @Transactional
    public void removeFoundPost(String atcId) {
        if (atcId != null && !atcId.trim().isEmpty()) {
            userMapper.deleteFoundPostByAtcId(atcId.trim());
        }
    }

    @Override
    @Transactional
    public void removeLostPost(String atcId) {
        if (atcId != null && !atcId.trim().isEmpty()) {
            userMapper.deleteLostPostByAtcId(atcId.trim());
        }
    }

    @Override
    public List<CommentVO> findAllCommentsByUserId(String userId){
        if (userId!=null && !userId.trim().isEmpty()){
            return userMapper.findAllCommentsByUserId(userId.trim());
        }
        return null;
    }

    @Override
    @Transactional // 🌟 중요: 두 테이블 삭제 작업 중 단 하나라도 실패하면 전부 원래대로 되돌리는 트랜잭션 보장
    public void deleteAllCommentsByUserId(String userId) {
        
        // 1. 내가 작성한 습득물 댓글 전체 삭제
        userMapper.deleteAllFoundCommentsByUserId(userId);
        
        // 2. 내가 작성한 분실물 댓글 전체 삭제
        userMapper.deleteAllLostCommentsByUserId(userId);
    }


    /**
     * 회원탈퇴
     * - 입력한 비밀번호와 DB 비밀번호 비교
     * - 일치할 시 is_deleted = 1, deleted_at = NOW()로 변경
     */
    @Override
    @Transactional
    public void withdrawUser(String id, String password) {

        // DB에서 현재 로그인한 회원 정보 조회
        UserVO dbUser = userMapper.findById(id);

        // 회원 정보가 없으면 예외 처리
        if (dbUser == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        // 입력한 비밀번호와 DB에 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(password, dbUser.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다. 다시 입력하세요.");
        }

        // 회원탈퇴 처리
        userMapper.withdrawUser(id);
    }
}