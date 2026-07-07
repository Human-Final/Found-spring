package com.human.found.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.chat.mapper.ChatMapper;
import com.human.found.domain.chat.vo.ChatFileVO;
import com.human.found.domain.chat.vo.ChatMessageVO;
import com.human.found.domain.chat.vo.ChatRoomVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
    
    private final ChatMapper chatMapper;
    
    // 채팅방 생성 또는 기존 채팅방 반환
    @Override
    public ChatRoomVO createChatRoom(ChatRoomVO chatRoom) {

        ChatRoomVO room = chatMapper.findChatRoom(
            chatRoom.getLostNum(),
            chatRoom.getFoundNum(),
            chatRoom.getUserIdA(),
            chatRoom.getUserIdB()
        );

        if (room != null) {
            return room;
        }

        chatMapper.insertChatRoom(chatRoom);

        return chatRoom;
    }

    // 내 채팅방 목록 조회
    @Override
    public List<ChatRoomVO> getMyChatRooms(String loginId) {
        return chatMapper.findMyChatRooms(loginId);
    }

    // 메시지 저장
    @Override
    public void saveMessage(ChatMessageVO message) {
        chatMapper.insertMessage(message);
    }

    // 채팅 내역 조회
    @Override
    public List<ChatMessageVO> getMessages(Long chatNum) {
        return chatMapper.findMessagesByChatNum(chatNum);
    }

    // 채팅방 번호로 채팅방 조회
    @Override
    public ChatRoomVO getChatRoomByChatNum(Long chatNum) {
        return chatMapper.findChatRoomByChatNum(chatNum);
    }

    // 습득 게시글의 채팅방 목록 조회
    @Override
    public List<ChatRoomVO> getRoomsByFoundNum(Long foundNum) {
        return chatMapper.findRoomsByFoundNum(foundNum);
    }

    // 분실 게시글의 채팅방 목록 조회
    @Override
    public List<ChatRoomVO> getRoomsByLostNum(Long lostNum) {
        return chatMapper.findRoomsByLostNum(lostNum);
    }

    // 습득 게시글 작성자 조회
    @Override
    public String getFoundWriter(Long foundNum) {
        return chatMapper.findFoundWriter(foundNum);
    }

    // 분실 게시글 작성자 조회
    @Override
    public String getLostWriter(Long lostNum) {
        return chatMapper.findLostWriter(lostNum);
    }

    // 습득 게시글 atcId 조회
    @Override
    public String getFoundAtcId(Long foundNum) {
        return chatMapper.findFoundAtcId(foundNum);
    }

    // 분실 게시글 atcId 조회
    @Override
    public String getLostAtcId(Long lostNum) {
        return chatMapper.findLostAtcId(lostNum);
    }

    // 채팅 첨부파일 저장
    @Override
    public void saveChatFile(ChatFileVO chatFile) {
        chatMapper.insertChatFile(chatFile);
    }
}