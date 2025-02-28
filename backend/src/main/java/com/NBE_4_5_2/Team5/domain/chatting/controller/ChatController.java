package com.NBE_4_5_2.Team5.domain.chatting.controller;

import com.NBE_4_5_2.Team5.domain.chatting.dto.ChatRoomRequest;
import com.NBE_4_5_2.Team5.domain.chatting.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chatting.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chatting.service.ChatService;
import com.NBE_4_5_2.Team5.domain.user.entity.Users;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/api/chatting")
@RequiredArgsConstructor
public class ChatController {
//    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    // 채팅방 생성
    @PostMapping("createRoom")
    @ResponseBody
    public ResponseEntity<RsData<ChatRoom>> createRoom(@RequestBody ChatRoomRequest request) {
        Optional<Users> sender=userService.getUserByUsername(request.getSenderName());
        Optional<Users> receiver=userService.getUserByUsername(request.getReceiverName());

        if(sender.isPresent() && receiver.isPresent()) {
            ChatRoom chatRoom=chatService.createRoom(sender.get(),receiver.get());
            return ResponseEntity.ok(new RsData<>("200","success",chatRoom));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RsData<>("404","user not found"));
        }

    }

    @GetMapping("{roomId}")
    public ResponseEntity<RsData<ChatRoom>> getChatRoom(@PathVariable String roomId) {
        ChatRoom chatRoom = chatService.findById(roomId);
        if (chatRoom == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RsData<>("404", "채팅방없음"));
        }
        return ResponseEntity.ok(new RsData<>("200", "success", chatRoom));
    }

//    @MessageMapping("/sendMessage")
//    public void sendMessage(ChatMessage incomingMessage) {
//        // roomId로 채팅방 조회
//        ChatRoom chatRoom = chatService.findById(incomingMessage.getChatRoomId());
//
//        if (chatRoom != null) {
//            // 발신자와 수신자 이름을 set
//            ChatMessage message = ChatMessage.builder()
//                    .chatRoomId(incomingMessage.getChatRoomId())
//                    .sender(chatRoom.getSender().getUsername()) // 발신자 이름
//                    .receiver(chatRoom.getReceiver().getUsername()) // 수신자 이름
//                    .message(incomingMessage.getMessage())
//                    .build();
//
//            // 채팅방에 메시지를 전송
//            messagingTemplate.convertAndSend("/topic/" + chatRoom.getId(), message);
//        }
//    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<RsData<List<ChatMessage>>> getMessages(@PathVariable String roomId) {

        List<ChatMessage> messageList=chatService.getMessagesByRoomId(roomId); // 특정 채팅방의 메시지 조회
        if(messageList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RsData<>("404","messages is null",messageList));
        }
        return ResponseEntity.ok(new RsData<>("200","success",messageList));
    }



}
