package com.human.found.domain.notice.service;

import com.human.found.domain.notice.vo.NoticeVO;
import java.util.List;

public interface NoticeService {
    List<NoticeVO> getNoticeList();
    List<NoticeVO> getActivePopups();
    NoticeVO getNoticeDetail(Long num); // 조회수 증가 포함
    void registerNotice(NoticeVO notice);
    void modifyNotice(NoticeVO notice);
    void removeNotice(Long num);

    
}
