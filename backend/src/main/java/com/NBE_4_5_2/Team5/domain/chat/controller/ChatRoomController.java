package com.NBE_4_5_2.Team5.domain.chat.controller;


import com.NBE_4_5_2.Team5.domain.chat.dto.ChatRoomDto;
import com.NBE_4_5_2.Team5.domain.chat.dto.MessageDto;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.entity.AccessProvider;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.service.AuthTokenService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final AuthTokenService authTokenService;
    private final ProductPostService productPostService;

    /*
    테스트용 HTML(임시)
     */
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

    // 사용자 토큰 조회
    @GetMapping("/user")
    @ResponseBody
    public RsData<AccessProvider> getUserInfo(HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies()); // 쿠키에서 액세스 토큰 가져오기
        String name = authTokenService.getUsernameFromToken(token); // 사용자 이름 가져오기
        AccessProvider access = AccessProvider.builder()
                .name(name)  // 사용자 이름
                .token(token)
                .build();
        return new RsData<>("200","success", access);
    }

    // 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    @Transactional
    public RsData<ChatRoom> createRoom(@RequestParam String postId,HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String sender = authTokenService.getUsernameFromToken(token);
        ProductPostResponse postResponse=productPostService.getPost(postId);

        String receiver = postResponse.getWriterName();
        ChatRoom chatRoom = chatRoomService.createChatRoom(sender,receiver);

        return new RsData<>("200",receiver+"와의 채팅방",chatRoom);
    }

    // 채팅방 조회
    @GetMapping("/rooms")
    @ResponseBody
    public RsData<List<ChatRoomDto>> getUserRooms(HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        List<ChatRoom> chatRoomsList=chatRoomService.findRoomByUser(username);

        List<ChatRoomDto> response=chatRoomsList.stream()
                .map(chatRoom -> new ChatRoomDto(
                        chatRoom.getId(),
                        chatRoom.getRoomId(),
                        chatRoom.getName(),
                        chatRoom.getUserCount()))
                .toList();


        return new RsData<>("200","채팅방 목록",response);
    }

    // 채팅방 메세지 조회
    @GetMapping("/message")
    @ResponseBody
    public RsData<List<MessageDto>> getMessages(HttpServletRequest request, @RequestParam String roomId) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        List<ChatMessage> messages= chatRoomService.getMessagesByUser(roomId,username);
        List<MessageDto> response=messages.stream()
                .map(chatMessage -> new MessageDto(
                        chatMessage.getSender(),
                        chatMessage.getMessage(),
                        chatMessage.getImage(),
                        chatMessage.getTimestamp()))
                .toList();
        return new RsData<>("200",roomId+"의 대화 목록",response);
    }

    // 채팅방 삭제
    @DeleteMapping("/message")
    @ResponseBody
    public RsData<String> deleteRoom(@RequestParam String roomId,HttpServletRequest request) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        chatRoomService.deleteChatRoom(roomId,username);
        return new RsData<>("200","삭제 완료");
    }

    // 특정 사용자와의 채팅방 검색
    @GetMapping("/search")
    @ResponseBody
    public RsData<String> findChatRooms(HttpServletRequest request,@RequestParam String receiver) {
        String token = authTokenService.getAccessTokenFromCookies(request.getCookies());
        String username = authTokenService.getUsernameFromToken(token);
        System.out.println("name:"+username);
        String roomId=chatRoomService.findByRoomIdByClients(username,receiver);

        if (roomId == null) {
            return new RsData<>("404", "존재하지 않는 대화방입니다.");
        }
        return new RsData<>("200","success","roomId: "+roomId);
    }

}