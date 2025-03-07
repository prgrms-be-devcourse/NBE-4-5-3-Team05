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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final AuthTokenService authTokenService;

//
//    @GetMapping("/room")
//    public String rooms() {
//        return "/chat/room";
//    }

    @GetMapping("/all/room")
    public String rooms(HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        if (token == null || authTokenService.getPayload(token) == null) {
            return "redirect:/api/users/login"; // 로그인 페이지로 리다이렉트
        }
        return "/chat/room";
    }

    @GetMapping("/all/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        List<ChatRoom> chatRooms = chatRoomService.findAllRoom();
        chatRooms.stream().forEach(room -> room.setUserCount(chatRoomService.getUserCount(room.getRoomId())));
        return chatRooms;
    }

    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String receiver,HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String sender = authTokenService.getUsernameFromToken(token);
        ChatRoom chatRoom = chatRoomService.createChatRoom(sender,receiver);
        return chatRoom;
    }

    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> getUserRooms(HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        System.out.println("token: " + token);
        String username = authTokenService.getUsernameFromToken(token);
        System.out.println("이름이름이름이름이름이름이름이름이름이름이름이름이름이름이름이름이름ㅍ:"+username);
        return chatRoomService.getRoomByUser(username);
    }

    @GetMapping("/room")
    @ResponseBody
    public List<ChatMessage> getMessages(HttpServletRequest request, @RequestParam String roomId) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        System.out.println("현재 사용자:"+username);
        return chatRoomService.getMessagesByUser(roomId,username);
    }

    @DeleteMapping("room")
    @ResponseBody
    public void deleteRoom(@RequestParam String roomId) {
        chatRoomService.deleteChatRoom(roomId);
    }


    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomService.findRoomById(roomId);
    }


    // 사용자 정보
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
}