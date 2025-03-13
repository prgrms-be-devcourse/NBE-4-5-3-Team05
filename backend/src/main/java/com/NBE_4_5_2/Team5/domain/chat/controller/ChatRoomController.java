package com.NBE_4_5_2.Team5.domain.chat.controller;


import com.NBE_4_5_2.Team5.domain.chat.dto.ChatRoomDto;
import com.NBE_4_5_2.Team5.domain.chat.dto.MessageDto;
import com.NBE_4_5_2.Team5.domain.chat.entity.AccessProvider;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.service.AuthTokenService;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.dto.Empty;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
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
    private final Rq rq;
    private final UserService userService;
    private final UserRepository userRepository;

    /*
    테스트용 HTML(임시)
     */
    // 모든 방 조회
    @GetMapping("/room")
    public String rooms() {
        String token=rq.getValueFromCookie("accessToken");
        if (token == null || authTokenService.getUsernameFromToken(token) == null) {
            return "redirect:/api/users/login"; // 로그인 페이지로 리다이렉트
        }
        return "/chat/room";
    }

    // 채팅방 상세 페이지로 이동 (HTML 반환)
    @GetMapping("/room/{roomId}/show")
    public String showRoomDetailPage(@PathVariable String roomId) {
        String token=rq.getValueFromCookie("accessToken");
        if (token == null || authTokenService.getUsernameFromToken(token) == null) {
            return "redirect:/api/users/login"; // 로그인 페이지로 리다이렉트
        }
        // roomId를 이용해 추가적인 방 정보 검증이나 처리 로직 추가 가능
        return "/chat/roomdetail"; // 채팅방 상세 페이지 반환
    }

    // 사용자 토큰 조회
    @GetMapping("/user")
    @ResponseBody
    public RsData<AccessProvider> getUserInfo() {
        String token=rq.getValueFromCookie("accessToken");
        User userIdentity = rq.getUserIdentity();
        User user = rq.getRealActor(userIdentity);

        AccessProvider access = AccessProvider.builder()
                .name(user.getNickname())  // 사용자 이름
                .token(token)
                .build();

        return new RsData<>("200","success", access);
    }

    // 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    @Transactional
    public RsData<ChatRoom> createRoom(@RequestParam String postId) {
        User userIdentity = rq.getUserIdentity();
        User sender = rq.getRealActor(userIdentity);

        ProductPostResponse postResponse=productPostService.getPost(postId);
        String receiver = postResponse.getWriterName();
        ChatRoom chatRoom = chatRoomService.createChatRoom(sender.getNickname(),receiver);

        return new RsData<>("200",receiver+"와의 채팅방",chatRoom);
    }

    // 고객센터
    @PostMapping("/admin/{adminId}")
    @ResponseBody
    @Transactional
    public RsData<ChatRoom> createRoomAdmin(@PathVariable String adminId) {
        User userIdentity = rq.getUserIdentity();
        User sender = rq.getRealActor(userIdentity);

        User admin = userService.getUserById(adminId).orElseThrow(
                ()-> new ServiceException("404","잘못된 ID")
        );

        if (!admin.isAdmin()) {
            throw new ServiceException("404", "옳지 않은 사용자"); // 권한 없음 예외
        }

        String receiver = admin.getNickname();

        ChatRoom chatRoom = chatRoomService.createChatRoom(sender.getNickname(),receiver);

        return new RsData<>("200","고객센터",chatRoom);
    }

    // 채팅방 조회
    @GetMapping("/rooms")
    @ResponseBody
    public RsData<List<ChatRoomDto>> getUserRooms() {
        User userIdentity = rq.getUserIdentity();
        User user = rq.getRealActor(userIdentity);

        List<ChatRoom> chatRoomsList=chatRoomService.findRoomByUser(user.getNickname());
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
    public RsData<List<MessageDto>> getMessages(@RequestParam String roomId) {
        User userIdentity = rq.getUserIdentity();
        User user = rq.getRealActor(userIdentity);
        List<ChatMessage> messages= chatRoomService.getMessagesByUser(roomId,user.getNickname());
        String other=chatRoomService.findOther(roomId,user.getNickname());
        List<MessageDto> response=messages.stream()
                .map(chatMessage -> new MessageDto(
                        chatMessage.getMessageId(),
                        chatMessage.getSender(),
                        chatMessage.getMessage(),
                        chatMessage.getImage(),
                        chatMessage.getTimestamp()))
                .toList();
        return new RsData<>("200",other+"와의 대화방",response);
    }

    // 채팅방 삭제
    @DeleteMapping("/message")
    @ResponseBody
    public RsData<?> deleteRoom(@RequestParam String roomId) {
        User userIdentity = rq.getUserIdentity();
        User user = rq.getRealActor(userIdentity);
        chatRoomService.deleteChatRoom(roomId,user.getNickname());
        return new RsData<>("200","삭제 완료",new Empty());
    }

    // 특정 사용자와의 채팅방 검색
    @GetMapping("/search")
    @ResponseBody
    public RsData<String> findChatRooms(@RequestParam String receiver) {
        User userIdentity = rq.getUserIdentity();
        User user = rq.getRealActor(userIdentity);
        System.out.println("name:"+user.getNickname());
        String roomId=chatRoomService.findByRoomIdByClients(user.getNickname(),receiver);

        if (roomId == null) {
            return new RsData<>("404", "존재하지 않는 대화방입니다.");
        }
        return new RsData<>("200","success","roomId: "+roomId);
    }

    // 권한부여(임시)
    @PutMapping("/admin")
    @ResponseBody
    @Transactional
    public RsData<UserDto> grantAdmin(@RequestParam String userId) {
        User user=userService.getUserById(userId).orElseThrow(()->new ServiceException("404","존재하지 않는 사용자"));
        user.setAdmin();
        userRepository.save(user);
        UserDto userDto=new UserDto(user);
        return new RsData<>("200","권한부여",userDto);
    }
}
