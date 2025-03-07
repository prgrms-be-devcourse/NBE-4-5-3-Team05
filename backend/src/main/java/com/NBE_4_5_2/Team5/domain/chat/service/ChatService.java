package com.NBE_4_5_2.Team5.domain.chat.service;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
            // 타임스탬프 설정
            chatMessage.setTimestamp(chatMessage.formatTimestamp(LocalDateTime.now()));
            // redis로 메세지 발송
            redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);

            // 퇴장
        } else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 나갔습니다.");
            chatMessage.setSender("[알림]");
            // 타임스탬프 설정
            chatMessage.setTimestamp(chatMessage.formatTimestamp(LocalDateTime.now()));
            // redis로 메세지 발송
            redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);

            // 이미지
        }else if (ChatMessage.MessageType.IMAGE.equals(chatMessage.getType())) {
            chatMessage.setMessage("");
            // 타임스탬프 설정
            chatMessage.setTimestamp(chatMessage.formatTimestamp(LocalDateTime.now()));
            // redis로 메세지 발송
            redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);

        } else if (ChatMessage.MessageType.TALK.equals(chatMessage.getType())) {
            // redis로 메세지 발송
            redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);

            // 현재 방에 참가중인 참여자 조회
            List<String> participants=chatRoomService.getParticipants(chatMessage.getRoomId());

            // 각 클라이언트의 개별 저장소
            for (String participant : participants) {
                ChatMessage message = ChatMessage.builder()
                        .type(ChatMessage.MessageType.TALK)
                        .roomId(chatMessage.getRoomId())
                        .client(participant) // 참가자 클라이언트를 저장
                        .sender(chatMessage.getSender())
                        .message(chatMessage.getMessage())
                        .userCount(chatRoomService.getUserCount(chatMessage.getRoomId()))
                        .image(chatMessage.getImage())
                        .timestamp(chatMessage.formatTimestamp(LocalDateTime.now()))
                        .build();
                // DB에 저장
                messageRepository.save(message);
            }

        }
    }

    // 개별 저장소 메세지 삭제
    public void deleteMessageByClient(String username, String roomId) {
        List<ChatMessage> messages=messageRepository.findAllByClientAndRoomId(username,roomId);
        System.out.println("삭제할 메세지들:"+messages);
        if (messages != null && !messages.isEmpty()) {
            System.out.println("삭제전");
            messageRepository.deleteAll(messages);
            System.out.println("삭제후");
        } else {
            System.out.println("삭제할 메시지가 없습니다.");
        }
    }


}