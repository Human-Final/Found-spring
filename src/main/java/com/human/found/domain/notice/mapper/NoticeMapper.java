package com.human.found.domain.notice.mapper;

import com.human.found.domain.notice.vo.NoticeFileVO;
import com.human.found.domain.notice.vo.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoticeMapper {
    List<NoticeVO> selectNoticeList();       // 목록 조회 (중요공지 상단 고정)
    List<NoticeVO> selectActivePopupList();  // 팝업 공지 리스트 조회
    NoticeVO selectNoticeDetail(Long num);    // 상세보기, 수정할 때 해당 내용 갖고오기
    void updateViewCount(Long num);          // 조회수 증가
    void insertNotice(NoticeVO notice);      // 작성
    void updateNotice(NoticeVO notice);      // 수정
    void deleteNotice(Long num);             // 삭제 (논리 삭제 처리)

    //이미지 추가, 삭제 매핑
    void insertNoticeFile(NoticeFileVO fileVO);
    NoticeFileVO selectNoticeFileByNum(Long num);
    void deleteNoticeFile(Long num);
}
