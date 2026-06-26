package com.human.found.domain.user.service;

import java.util.List;

import com.human.found.domain.comment.vo.CommentVO;
import com.human.found.domain.user.vo.MyPagePostVO;
import com.human.found.domain.user.vo.UserVO;

import jakarta.servlet.http.HttpSession;

/**
 * 회원 관련 서비스 인터페이스
 * - Controller가 직접 구현체를 알지 않도록 기능 목록만 정의
 * - 실제 로직은 UserServiceImpl에서 구현
 */
public interface UserService {

    /**
     * 회원가입
     * - 비밀번호 암호화
     * - 회원 정보 저장
     */
    void join(UserVO user);

    /**
     * 아이디로 회원 조회
     * 로그인 시 Security에서 사용
     */
    UserVO findById(String id);

    // 사용자 아이디 조회
    String findUserId(String name, String email);

    // 사용자 정보3가지로 존재하는 유저인지 확인하기
    boolean isUserExist(String id,String name,String email);

    // 사용자 비밀번호 조회 이메일 인증
    String sendPwEmail(String email, HttpSession session);

    // 사용자 비밀번호 조회 이메일 인증코드 확인하기
    String verifyPwCode(String inputCode, String email, HttpSession session);

    /**
     * 아이디 중복 확인
     * true : 중복
     * false : 사용 가능
     */
    boolean isDuplicatedId(String id);

    /**
     * 이메일 중복 확인
     * true : 중복
     * false : 사용 가능
     */
    boolean isDuplicatedEmail(String email);

    // 회원가입 이메일 인증코드 전송하기
    public String sendJoinEmail(String email, HttpSession session);

    // 회원가입 인증코드 알맞은지 확인하기
    public String verifyJoinCode(String inputCode, String email, HttpSession session);

    /**
     * 회원가입 입력값 검증
     */
    String validateJoin(UserVO user);

    /*
    비밀번호 본인 인증 판정
    */
    boolean checkPassword(String id, String pwCheck);

    /*
    비밀번호 수정하기
    */
    public void updateUserPassword(String id, String newPw);

    /**
     * 회원정보 수정
     * - 회원 정보 업데이트
     */
    void updateUserInfo(UserVO userVO);
    
    /**
     * 마이페이지 뷰를 띄울 때 회원 정보를 안전하게 꺼내오기 위한 메서드
     */
    UserVO getUserInfo(String id);

    // 사용자가 작성한 글의 갯수 갖고오기 메서드
    java.util.Map<String, Object> getMyPageCounts(String userId);

    // 사용자 작성 게시글 모두 갖고오기 메서드
    List<MyPagePostVO> getMyAllPostList(String userId);

    // 사용자 최신 게시글 2개만 갖고오기 메서드
    List<MyPagePostVO> getMyRecentPostList(String userId);

    // 사용자 게시글 삭제 메서드
    void removeFoundPost(String atcId);
    void removeLostPost(String atcId);

    // 사용자 작성 댓글 모두 갖고오기 메서드
    List<CommentVO> findAllCommentsByUserId(String userId);

    // 사용자 작성 댓글 모두 삭제하기 메서드
    void deleteAllCommentsByUserId(String UserId);

    /**
     * 회원탈퇴
     * 비밀번호 확인 후 소프트 삭제 처리
     */
    void withdrawUser(String id, String password);

    



}