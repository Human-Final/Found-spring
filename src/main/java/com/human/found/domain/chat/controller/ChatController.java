package com.human.found.domain.chat.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.human.found.domain.chat.service.ChatService;
import com.human.found.domain.chat.vo.ChatMessageVO;
import com.human.found.domain.chat.vo.ChatRoomVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 습득 게시글 채팅 버튼
     * - 게시글 작성자: 해당 게시글에 들어온 전체 채팅방 목록으로 이동
     * - 다른 사용자: 게시글 작성자와의 1:1 채팅방 생성 또는 기존 채팅방 입장
     */
    @PostMapping("/chat/found/{num}")
    public String createFoundChatRoom(@PathVariable Long num,
                                      Principal principal) {

        String loginId = principal.getName();

        // ChatMapper에서 습득 게시글 작성자만 조회
        String writerId = chatService.getFoundWriter(num);

        if (writerId == null) {
            return "redirect:/api/found";
        }

        // 게시글 작성자 본인은 채팅방 목록으로 이동
        if (writerId.equals(loginId)) {
            return "redirect:/chat/found/" + num + "/rooms";
        }

        ChatRoomVO chatRoom = new ChatRoomVO();
        chatRoom.setFoundNum(num);
        chatRoom.setLostNum(null);
        chatRoom.setUserIdA(writerId); // 게시글 작성자
        chatRoom.setUserIdB(loginId);  // 채팅 신청자

        ChatRoomVO room = chatService.createChatRoom(chatRoom);

        // 다른 사용자는 본인의 1:1 채팅방으로 바로 이동
        return "redirect:/chat/room/" + room.getChatNum();
    }

    /**
     * 분실 게시글 채팅 버튼
     * - 게시글 작성자: 해당 게시글에 들어온 전체 채팅방 목록으로 이동
     * - 다른 사용자: 게시글 작성자와의 1:1 채팅방 생성 또는 기존 채팅방 입장
     */
    @PostMapping("/chat/lost/{num}")
    public String createLostChatRoom(@PathVariable Long num,
                                     Principal principal) {

        String loginId = principal.getName();

        // ChatMapper에서 분실 게시글 작성자만 조회
        String writerId = chatService.getLostWriter(num);

        if (writerId == null) {
            return "redirect:/api/lost";
        }

        // 게시글 작성자 본인은 채팅방 목록으로 이동
        if (writerId.equals(loginId)) {
            return "redirect:/chat/lost/" + num + "/rooms";
        }

        ChatRoomVO chatRoom = new ChatRoomVO();
        chatRoom.setLostNum(num);
        chatRoom.setFoundNum(null);
        chatRoom.setUserIdA(writerId); // 게시글 작성자
        chatRoom.setUserIdB(loginId);  // 채팅 신청자

        ChatRoomVO room = chatService.createChatRoom(chatRoom);

        // 다른 사용자는 본인의 1:1 채팅방으로 바로 이동
        return "redirect:/chat/room/" + room.getChatNum();
    }

    /**
     * 습득 게시글에 연결된 채팅방 목록
     * - 게시글 작성자만 전체 채팅방 목록을 볼 수 있다.
     */
    @GetMapping("/chat/found/{num}/rooms")
    public String foundChatRooms(@PathVariable Long num,
                                 Model model,
                                 Principal principal) {

        String loginId = principal.getName();

        // 게시글 작성자 조회
        String writerId = chatService.getFoundWriter(num);

        if (writerId == null) {
            return "redirect:/api/found";
        }

        // 작성자가 아닌 사용자는 전체 채팅 목록 접근 차단
        if (!writerId.equals(loginId)) {
            String atcId = chatService.getFoundAtcId(num);
            return "redirect:/api/found/detail/" + atcId;
        }

        List<ChatRoomVO> chatRooms = chatService.getRoomsByFoundNum(num);

        model.addAttribute("loginId", loginId);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("boardType", "found");
        model.addAttribute("boardNum", num);

        return "chat/list";
    }

    /**
     * 분실 게시글에 연결된 채팅방 목록
     * - 게시글 작성자만 전체 채팅방 목록을 볼 수 있다.
     */
    @GetMapping("/chat/lost/{num}/rooms")
    public String lostChatRooms(@PathVariable Long num,
                                Model model,
                                Principal principal) {

        String loginId = principal.getName();

        // 게시글 작성자 조회
        String writerId = chatService.getLostWriter(num);

        if (writerId == null) {
            return "redirect:/api/lost";
        }

        // 작성자가 아닌 사용자는 전체 채팅 목록 접근 차단
        if (!writerId.equals(loginId)) {
            String atcId = chatService.getLostAtcId(num);
            return "redirect:/api/lost/detail/" + atcId;
        }

        List<ChatRoomVO> chatRooms = chatService.getRoomsByLostNum(num);

        model.addAttribute("loginId", loginId);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("boardType", "lost");
        model.addAttribute("boardNum", num);

        return "chat/list";
    }

    /**
     * 실제 1:1 채팅방 입장
     * - 채팅방 참여자만 입장 가능
     */
    @GetMapping("/chat/room/{chatNum}")
    public String chatRoom(@PathVariable Long chatNum,
                           Model model,
                           Principal principal) {

        String loginId = principal.getName();

        ChatRoomVO room = chatService.getChatRoomByChatNum(chatNum);

        if (room == null) {
            return "redirect:/mypage";
        }

        // 채팅방 참여자가 아닌 사용자는 접근 차단
        if (!loginId.equals(room.getUserIdA()) && !loginId.equals(room.getUserIdB())) {
            return "redirect:/mypage";
        }

        List<ChatMessageVO> messages = chatService.getMessages(chatNum);

        model.addAttribute("chatNum", chatNum);
        model.addAttribute("loginId", loginId);
        model.addAttribute("room", room);
        model.addAttribute("messages", messages);

        return "chat/chat";
    }
}