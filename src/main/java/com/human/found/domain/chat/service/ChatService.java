package com.human.found.domain.chat.service;

import java.util.List;

import com.human.found.domain.chat.vo.ChatFileVO;
import com.human.found.domain.chat.vo.ChatMessageVO;
import com.human.found.domain.chat.vo.ChatRoomVO;

public interface ChatService {

    // 채팅방 생성 또는 조회
    ChatRoomVO createChatRoom(ChatRoomVO chatRoom);

    // 내 채팅방 목록
    List<ChatRoomVO> getMyChatRooms(String loginId);

    // 메시지 저장
    void saveMessage(ChatMessageVO message);

    // 채팅 내역 조회
    List<ChatMessageVO> getMessages(Long chatNum);

    // 채팅방 번호로 채팅방 조회
    ChatRoomVO getChatRoomByChatNum(Long chatNum);

    // 습득 게시글의 채팅방 목록 조회
    List<ChatRoomVO> getRoomsByFoundNum(Long foundNum);

    // 분실 게시글의 채팅방 목록 조회
    List<ChatRoomVO> getRoomsByLostNum(Long lostNum);

    // 습득 게시글 작성자 조회
    String getFoundWriter(Long foundNum);

    // 분실 게시글 작성자 조회
    String getLostWriter(Long lostNum);

    // 습득 게시글 atcId 조회
    String getFoundAtcId(Long foundNum);

    // 분실 게시글 atcId 조회
    String getLostAtcId(Long lostNum);

    // 채팅 첨부파일 저장
    void saveChatFile(ChatFileVO chatFile);

    // 채팅방 삭제
    void deleteChatRooms(List<Long> chatNums);

    // 채팅방 읽음 처리
    void readChatRoom(Long chatNum, String loginId);
}