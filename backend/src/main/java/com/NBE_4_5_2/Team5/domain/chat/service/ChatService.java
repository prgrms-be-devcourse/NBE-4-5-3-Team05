package com.NBE_4_5_2.Team5.domain.chat.service;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
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
        // 입장
        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에 입장했습니다.");
            chatMessage.setSender("[알림]");
        // 퇴장
        } else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 나갔습니다.");
            chatMessage.setSender("[알림]");
        // 이미지
        }else if (ChatMessage.MessageType.IMAGE.equals(chatMessage.getType())) {
            chatMessage.setMessage("");
        }
        // 타임스탬프 설정
        chatMessage.setTimestamp(chatMessage.formatTimestamp(LocalDateTime.now()));
        // redis로 메세지 발송
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
        // DB에 저장
        messageRepository.save(chatMessage);
    }


}