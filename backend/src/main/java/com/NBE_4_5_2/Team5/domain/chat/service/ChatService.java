package com.NBE_4_5_2.Team5.domain.chat.service;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageRepository messageRepository;

    /**
     * destination정보에서 roomId 추출
     */
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

    /**
     * 채팅방에 메시지 발송
     */
    public void sendChatMessage(ChatMessage chatMessage) {
        chatMessage.setUserCount(chatRoomService.getUserCount(chatMessage.getRoomId()));
        List<ChatRoom> chatRooms=chatRoomService.findByRoomId(chatMessage.getRoomId());
        // roomId에 해당하는 방이 하나만 존재
        if(chatRooms.size()==1){
            String receiver = chatRoomService.findOther(chatMessage.getRoomId(), chatMessage.getSender());

            String sender = chatMessage.getSender();
            ChatRoom chatRoom=chatRoomService.createChatRoom(receiver,sender);
            chatRooms.add(chatRoom);
        }

        for(ChatRoom chatRoom : chatRooms) {
            if (ChatMessage.MessageType.TALK.equals(chatMessage.getType())) {

                // redis로 메세지 발송
                objectRedisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
                ChatMessage message=ChatMessage.builder()
                        .type(ChatMessage.MessageType.TALK)
                        .roomId(chatRoom.getRoomId()) // 동일한 roomId 사용
                        .client(chatRoom.getId()) // 클라이언트에 맞춰 설정
                        .sender(chatMessage.getSender()) // 원 메시지의 발신자
                        .message(chatMessage.getMessage()) // 원 메시지 내용
                        .userCount(chatMessage.getUserCount())
                        .image(chatMessage.getImage())
                        .timestamp(chatMessage.formatTimestamp(LocalDateTime.now()))
                        .build();
                // DB에 저장
                messageRepository.save(message);

            }else if (ChatMessage.MessageType.IMAGE.equals(chatMessage.getType())) {
                chatMessage.setMessage("");

                ChatMessage message=ChatMessage.builder()
                        .type(ChatMessage.MessageType.IMAGE)
                        .roomId(chatRoom.getRoomId()) // 동일한 roomId 사용
                        .client(chatRoom.getId()) // 클라이언트에 맞춰 설정
                        .sender(chatMessage.getSender()) // 원 메시지의 발신자
                        .message(chatMessage.getMessage()) // 원 메시지 내용
                        .userCount(chatMessage.getUserCount())
                        .image(chatMessage.getImage())
                        .timestamp(chatMessage.formatTimestamp(LocalDateTime.now()))
                        .build();

                // redis로 메세지 발송
                objectRedisTemplate.convertAndSend(channelTopic.getTopic(), message);
                messageRepository.save(message);

            }else if (ChatMessage.MessageType.LOCATION.equals(chatMessage.getType())) {
                // LOCATION 타입 처리를 위한 코드 추가
                ChatMessage message = ChatMessage.builder()
                        .type(ChatMessage.MessageType.LOCATION)
                        .roomId(chatRoom.getRoomId()) // 동일한 roomId 사용
                        .client(chatRoom.getId()) // 클라이언트에 맞춰 설정
                        .sender(chatMessage.getSender()) // 원 메시지의 발신자
                        .message("위치 전송") // 메시지 내용
                        .userCount(chatMessage.getUserCount())
                        .latitude(chatMessage.getLatitude()) // 위도 설정
                        .longitude(chatMessage.getLongitude()) // 경도 설정
                        .timestamp(chatMessage.formatTimestamp(LocalDateTime.now()))
                        .build();

                // Redis로 메시지 발송
                objectRedisTemplate.convertAndSend(channelTopic.getTopic(), message);
                messageRepository.save(message);
            }
        }
    }


}