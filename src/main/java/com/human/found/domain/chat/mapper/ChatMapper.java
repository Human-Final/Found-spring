package com.human.found.domain.chat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.human.found.domain.chat.vo.ChatFileVO;
import com.human.found.domain.chat.vo.ChatMessageVO;
import com.human.found.domain.chat.vo.ChatRoomVO;

@Mapper
public interface ChatMapper {

    // 채팅방 중복 확인
    ChatRoomVO findChatRoom(@Param("lostNum") Long lostNum,
                            @Param("foundNum") Long foundNum,
                            @Param("userIdA") String userIdA,
                            @Param("userIdB") String userIdB);

    // 채팅방 생성
    void insertChatRoom(ChatRoomVO chatRoom);

    // 채팅방 번호로 조회
    ChatRoomVO findChatRoomByChatNum(@Param("chatNum") Long chatNum);

    // 내 채팅방 목록
    List<ChatRoomVO> findMyChatRooms(@Param("loginId") String loginId);

    // 메시지 저장
    void insertMessage(ChatMessageVO message);

    // 채팅방 메시지 목록
    List<ChatMessageVO> findMessagesByChatNum(@Param("chatNum") Long chatNum);

    // 습득 게시글에 연결된 채팅방 목록 조회
    List<ChatRoomVO> findRoomsByFoundNum(@Param("foundNum") Long foundNum);

    // 분실 게시글에 연결된 채팅방 목록 조회
    List<ChatRoomVO> findRoomsByLostNum(@Param("lostNum") Long lostNum);

    // 습득 게시글 작성자 아이디 조회
    String findFoundWriter(@Param("foundNum") Long foundNum);

    // 분실 게시글 작성자 아이디 조회
    String findLostWriter(@Param("lostNum") Long lostNum);

    // 습득 게시글 atcId 조회
    String findFoundAtcId(@Param("foundNum") Long foundNum);

    // 분실 게시글 atcId 조회
    String findLostAtcId(@Param("lostNum") Long lostNum);

    // 채팅 첨부파일 저장
    void insertChatFile(ChatFileVO chatFile);

    // 메시지 번호로 첨부파일 목록 조회
    List<ChatFileVO> findFilesByMessageNum(@Param("messageNum") Long messageNum);

    // 채팅방 삭제
    void deleteChatRooms(@Param("chatNums") List<Long> chatNums);

    // 채팅방 입장 시 읽음 처리
    void updateLastReadMessage(@Param("chatNum") Long chatNum,
                               @Param("loginId") String loginId,
                               @Param("lastMessageNum") Long lastMessageNum);

    // 마지막 메시지 번호
    Long findLastMessageNum(@Param("chatNum") Long chatNum);
}