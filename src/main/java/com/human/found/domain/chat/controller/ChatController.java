package com.human.found.domain.chat.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.chat.service.ChatService;
import com.human.found.domain.chat.vo.ChatFileVO;
import com.human.found.domain.chat.vo.ChatMessageVO;
import com.human.found.domain.chat.vo.ChatRoomVO;
import com.human.found.infrastructure.file.FileUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FileUtil fileUtil;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 습득 게시글 채팅 버튼
     * - 게시글 작성자: 해당 게시글에 들어온 전체 채팅방 목록으로 이동
     * - 다른 사용자: 게시글 작성자와의 1:1 채팅방 생성 또는 기존 채팅방 입장
     */
    @PostMapping("/chat/found/{num}")
    public String createFoundChatRoom(@PathVariable Long num,
                                      Principal principal) {

        String loginId = getLoginId(principal);

        // 습득 게시글 작성자 조회
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

        String loginId = getLoginId(principal);

        // 분실 게시글 작성자 조회
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

        String loginId = getLoginId(principal);

        // 습득 게시글 작성자 조회
        String writerId = chatService.getFoundWriter(num);

        if (writerId == null) {
            return "redirect:/api/found";
        }

        // 작성자가 아닌 사용자는 전체 채팅방 목록 접근 차단
        if (!writerId.equals(loginId)) {
            String atcId = chatService.getFoundAtcId(num);

            return "redirect:/api/found/detail/" + atcId;
        }

        List<ChatRoomVO> chatRooms =
                chatService.getRoomsByFoundNum(num);

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

        String loginId = getLoginId(principal);

        // 분실 게시글 작성자 조회
        String writerId = chatService.getLostWriter(num);

        if (writerId == null) {
            return "redirect:/api/lost";
        }

        // 작성자가 아닌 사용자는 전체 채팅방 목록 접근 차단
        if (!writerId.equals(loginId)) {
            String atcId = chatService.getLostAtcId(num);

            return "redirect:/api/lost/detail/" + atcId;
        }

        List<ChatRoomVO> chatRooms =
                chatService.getRoomsByLostNum(num);

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

        String loginId = getLoginId(principal);

        ChatRoomVO room =
                chatService.getChatRoomByChatNum(chatNum);

        if (room == null) {
            return "redirect:/mypage";
        }

        // 채팅방 참여자가 아닌 사용자는 접근 차단
        if (!isChatRoomParticipant(room, loginId)) {
            return "redirect:/mypage";
        }

        List<ChatMessageVO> messages =
                chatService.getMessages(chatNum);

        model.addAttribute("chatNum", chatNum);
        model.addAttribute("loginId", loginId);
        model.addAttribute("room", room);
        model.addAttribute("messages", messages);

        return "chat/chat";
    }

    /**
     * 채팅 첨부파일 업로드
     * - 채팅방 참여자 검증
     * - 파일 메시지를 chat_message에 저장
     * - 저장된 message_num으로 chat_file 저장
     * - 저장된 메시지를 JSON으로 반환
     */
    @PostMapping("/chat/file/upload")
    @ResponseBody
    public ChatMessageVO uploadChatFile(
            @RequestParam("chatNum") Long chatNum,
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        // 인증된 사용자 아이디를 발신자 아이디로 사용
        String senderId = getLoginId(principal);

        // 빈 파일 요청 차단
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "업로드할 파일이 없습니다."
            );
        }

        // 메시지 저장 전에 채팅방 존재 및 참여자 여부 검증
        validateChatRoomParticipant(chatNum, senderId);

        // 1. 채팅 메시지 먼저 저장
        ChatMessageVO message = new ChatMessageVO();
        message.setChatNum(chatNum);
        message.setSenderId(senderId);
        message.setContent(file.getOriginalFilename());

        chatService.saveMessage(message);

        // 2. 실제 파일 저장
        MultipartFile[] files = {file};

        List<Map<String, Object>> uploadedFiles =
                fileUtil.uploadFiles(
                        files,
                        String.valueOf(message.getMessageNum()),
                        "chat"
                );

        if (uploadedFiles == null || uploadedFiles.isEmpty()) {
            throw new IllegalStateException(
                    "파일 저장에 실패했습니다."
            );
        }

        Map<String, Object> fileInfo = uploadedFiles.get(0);

        // 3. chat_file 테이블 저장
        ChatFileVO chatFile = new ChatFileVO();
        chatFile.setMessageNum(message.getMessageNum());
        chatFile.setOriginalName(
                (String) fileInfo.get("originalname")
        );
        chatFile.setSaveName(
                (String) fileInfo.get("saveFileName")
        );
        chatFile.setFileSize(
                (Long) fileInfo.get("fileSize")
        );

        // 브라우저에서 접근할 파일 경로
        chatFile.setFilePath(
                "/images/chat/" + fileInfo.get("saveFileName")
        );

        chatService.saveChatFile(chatFile);

        // 4. 화면 출력용 첨부파일 목록 설정
        List<ChatFileVO> fileList = new ArrayList<>();
        fileList.add(chatFile);

        message.setFileList(fileList);

        return message;
    }

    /**
     * 마이페이지에서 채팅방 삭제
     */
    @PostMapping("/mypage/chat/delete")
    public String deleteChatRooms(
            @RequestParam List<Long> chatNums) {

        chatService.deleteChatRooms(chatNums);

        return "redirect:/mypage#myChats";
    }

    /**
     * 채팅방 입장 시 마지막 메시지까지 읽음 처리
     */
    @PostMapping("/chat/room/{chatNum}/read")
    @ResponseBody
    public void readChatRoom(@PathVariable Long chatNum,
                             Principal principal) {

        String loginId = getLoginId(principal);

        // 읽음 처리 전에 채팅방 참여자 여부 검증
        validateChatRoomParticipant(chatNum, loginId);

        chatService.readChatRoom(chatNum, loginId);

        // 같은 채팅방 구독자에게 읽음 이벤트 전송
        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + chatNum + "/read",
                loginId
        );
    }

    /**
     * 현재 인증된 사용자의 아이디를 반환하는 공통 메서드
     */
    private String getLoginId(Principal principal) {

        if (principal == null) {
            throw new AccessDeniedException(
                    "로그인이 필요한 기능입니다."
            );
        }

        return principal.getName();
    }

    /**
     * 로그인 사용자가 채팅방 참여자인지 확인
     */
    private boolean isChatRoomParticipant(
            ChatRoomVO room,
            String loginId) {

        return loginId.equals(room.getUserIdA())
                || loginId.equals(room.getUserIdB());
    }

    /**
     * 채팅방 존재 여부와 참여자 여부를 검증
     */
    private ChatRoomVO validateChatRoomParticipant(
            Long chatNum,
            String loginId) {

        ChatRoomVO room =
                chatService.getChatRoomByChatNum(chatNum);

        if (room == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 채팅방입니다."
            );
        }

        if (!isChatRoomParticipant(room, loginId)) {
            throw new AccessDeniedException(
                    "해당 채팅방에 접근할 권한이 없습니다."
            );
        }

        return room;
    }
}