package com.human.found.domain.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.comment.vo.CommentVO;
import com.human.found.domain.user.vo.MyPagePostVO;
import com.human.found.domain.user.vo.UserVO;

@Mapper
public interface UserMapper {
    // 아이디로 회원 조회
    UserVO findById(@Param("id")String id);

    // 사용자의 아이디 조회
    String findUserId(String name, String email);

    // 사용자 비밀번호 업데이트 실행
    int updatePassword(@Param("userId") String userId, 
                       @Param("encodedPassword") String encodedPassword);

    // 회원가입
    void insertUser(UserVO user);

    // 아이디 중복검사
    int countById(@Param("id") String id);
    
    // 이메일 중복 확인
    int countByEmail(@Param("email") String email);
    
    // 회원정보 수정할 때 사용하는 쿼리매핑
    void updateUser(UserVO user);

    // 비밀번호만 단독 변경하는 쿼리
    int updatePasswordOnly(@Param("id") String id, @Param("pw") String pw);

    // 비밀번호 찾기 회원정보 유무 확인
    int isUserExist(String id, String name, String email);

    // 회원탈퇴 처리 (실제 삭제는 아니고 is_deleted, deleted_at변경)
    int withdrawUser(String id);

    // 유저의 게시글 갯수 갖고오기
    java.util.Map<String, Object> selectMyPageCounts(String userId);

    // 유저의 작성된 게시글 모두 갖고오기
    List<MyPagePostVO> selectMyAllPostList(String userId);

    // 유저의 작성된 최신 게시글 2개만 갖고오기
    List<MyPagePostVO> selectMyRecentPostList(String userId);

    // 유저 게시글 완료처리
    void doneFoundPost(String atcId);
    void doneLostPost(String atcId);

    // 유저의 게시글 삭제하기
    int deleteFoundPostByAtcId(String atcId);
    int deleteLostPostByAtcId(String atcId);

    // 유저의 작성한 모든 댓글 갖고오기
    List<CommentVO> findAllCommentsByUserId(String userId);

    // 유저의 모든 작성 댓글 삭제하기(2개모두)
    void deleteAllFoundCommentsByUserId(String UserId);
    void deleteAllLostCommentsByUserId(String UserId);

}
