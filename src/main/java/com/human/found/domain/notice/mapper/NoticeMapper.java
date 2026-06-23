package com.human.found.domain.notice.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.human.found.domain.notice.vo.NoticeFileVO;
import com.human.found.domain.notice.vo.NoticeVO;
import com.human.found.global.common.paging.PagingVO;

@Mapper
public interface NoticeMapper {
    List<NoticeVO> selectNoticeList(PagingVO pagingVO);       // 목록 조회 (중요공지 상단 고정)
    int selectNoticeCount(PagingVO pagingVO);   // 전체 페이지 알아보기
    List<NoticeVO> selectPopupNotices();  // 팝업 공지 리스트 조회
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
