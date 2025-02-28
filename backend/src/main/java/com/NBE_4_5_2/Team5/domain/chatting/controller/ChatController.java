package com.NBE_4_5_2.Team5.domain.chatting.controller;

import com.NBE_4_5_2.Team5.domain.chatting.dto.ChatRoomRequest;
import com.NBE_4_5_2.Team5.domain.chatting.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chatting.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chatting.service.ChatService;
import com.NBE_4_5_2.Team5.domain.user.entity.Users;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/chatting")
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    // 채팅방 생성
    @PostMapping("createRoom")
    @ResponseBody
    public RsData<ChatRoom> createRoom(@RequestBody ChatRoomRequest request) {
        Optional<Users> sender=userService.getUserByUsername(request.getSenderName());
        Optional<Users> receiver=userService.getUserByUsername(request.getReceiverName());

        if(sender.isPresent() && receiver.isPresent()) {
            ChatRoom chatRoom=chatService.createRoom(sender.get(),receiver.get());
            return new RsData<>("200","success",chatRoom);
        }else{
            return new RsData<>("400","user not found");
        }

    }

    @GetMapping("{roomId}")
    public RsData<ChatRoom> getChatRoom(@PathVariable String roomId) {
        ChatRoom chatRoom = chatService.findById(roomId);
        if(chatRoom == null) {
            return new RsData<>("404","채팅방없음");
        }
        return new RsData<>("200","success",chatRoom);

    }

    @MessageMapping("/send/{roomId}")
    @SendTo("/topic/messages/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable UUID roomId, ChatMessage message) {
        return message; // 전송된 메시지를 클라이언트에게 반환
    }

}
