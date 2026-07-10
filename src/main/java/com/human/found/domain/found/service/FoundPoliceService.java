package com.human.found.domain.found.service;

/**
 * 경찰청 습득물 API 관련 서비스 인터페이스
 * 1. 경찰청 API 호출
 * 2. XML 데이터 수신
 * 3. XML 파싱
 * 4. DB 저장
 */
public interface FoundPoliceService {

    // 경찰청 API 호출
    String savePoliceFoundItems();

}