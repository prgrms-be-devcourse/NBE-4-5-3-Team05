package com.NBE_4_5_2.Team5.domain.chat.controller;


import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.entity.LoginInfo;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.user.service.AuthTokenService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final AuthTokenService authTokenService;

    // 모든 방 조회
    @GetMapping("/room")
    public String rooms(HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        if (token == null || authTokenService.getPayload(token) == null) {
            return "redirect:/api/users/login"; // 로그인 페이지로 리다이렉트
        }
        return "/chat/room";
    }

    // 채팅방 상세 페이지로 이동 (HTML 반환)
    @GetMapping("/room/{roomId}/show")
    public String showRoomDetailPage(@PathVariable String roomId, HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        if (token == null || authTokenService.getPayload(token) == null) {
            return "redirect:/api/users/login"; // 로그인 페이지로 리다이렉트
        }
        // roomId를 이용해 추가적인 방 정보 검증이나 처리 로직 추가 가능
        return "/chat/roomdetail"; // 채팅방 상세 페이지 반환
    }

    //    모든 방 조회
    @GetMapping("/all/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        List<ChatRoom> chatRooms = chatRoomService.findAllRoom();
        chatRooms.stream().forEach(room -> room.setUserCount(chatRoomService.getUserCount(room.getRoomId())));
        return chatRooms;
    }

    // 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String receiver,HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String sender = authTokenService.getUsernameFromToken(token);
        ChatRoom chatRoom = chatRoomService.createChatRoom(sender,receiver);
        return chatRoom;
    }

    // 클라이언트의 채팅방 조회
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> getUserRooms(HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        System.out.println("token: " + token);
        String username = authTokenService.getUsernameFromToken(token);
        System.out.println("이름:"+username);
        return chatRoomService.getRoomByUser(username);
    }

    // 채팅방 메세지 조회
    @GetMapping("/message")
    @ResponseBody
    public List<ChatMessage> getMessages(HttpServletRequest request, @RequestParam String roomId) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        System.out.println("현재 사용자:"+username);
        return chatRoomService.getMessagesByUser(roomId,username);
    }

    // 채팅방 삭제
    @DeleteMapping("/message")
    @ResponseBody
    public void deleteRoom(@RequestParam String roomId,HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        chatRoomService.deleteChatRoom(roomId,username);
    }

    // 사용자 정보 조회
    @GetMapping("/user")
    @ResponseBody
    public RsData<LoginInfo> getUserInfo(HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies()); // 쿠키에서 액세스 토큰 가져오기
        System.out.println("token: " + token);
        String name = authTokenService.getUsernameFromToken(token); // 사용자 이름 가져오기
        System.out.println("name: " + name);
        LoginInfo loginInfo=LoginInfo.builder()
                .name(name)  // 사용자 이름
                .token(token)
                .build();
        return new RsData<>("200","success",loginInfo);
    }


    // 특정 roomId에 대한 채팅방 조회
    @GetMapping("/{roomId}")
    @ResponseBody
    public List<ChatRoom> getChatRoomsByRoomId(@PathVariable String roomId) {
        List<ChatRoom> chatRooms = chatRoomService.findByRoomId(roomId);
        if (chatRooms.isEmpty()) {
            throw new RuntimeException("없음");
        }
        return chatRooms;
    }

    // 특정 사용자들이 사용중인 채팅방 존재여부 검증
    @GetMapping()
    @ResponseBody
    public RsData<String> getChatRooms(HttpServletRequest request,@RequestParam String receiver) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        String roomId=chatRoomService.findByRoomIdByUsers(username,receiver);
        if(roomId==null) {
            return new RsData<>("200","success",null);
        }
        else{
            return new RsData<>("200","success",roomId);
        }
    }

}