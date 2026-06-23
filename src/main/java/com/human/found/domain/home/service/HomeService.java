package com.human.found.domain.home.service;

import java.util.List;

import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.lost.vo.LostVO;
import com.human.found.domain.notice.vo.NoticeVO;

public interface HomeService {

    // 메인화면 분실/습득 게시글 최신글 2개 출력
    public List<FoundVO> recentFoundList();
    public List<LostVO> recentLostList();

    // 메인화면 분실/습득 카운팅
    public long countWeeklyFound();
    public long countWeeklyLost();
    public long countWeeklyDone();

    // 총 갯수 카운팅
    public long countTotalFound();
    public long countTotalLost();
    
}
