package com.human.found.domain.notice.service;

import java.util.List;

import com.human.found.domain.notice.vo.NoticeVO;
import com.human.found.global.common.paging.PagingVO;

public interface NoticeService {
    List<NoticeVO> getNoticeList(PagingVO pagingVO);
    List<NoticeVO> getActivePopups();
    NoticeVO getNoticeDetail(Long num); // 조회수 증가 포함
    void registerNotice(NoticeVO notice);
    void modifyNotice(NoticeVO notice);
    void removeNotice(Long num, String image_path);

    NoticeVO getNoticeForEditandDelete(Long num);    //오로지 내용 수정, 삭제만을 위한 메서드

    
}
