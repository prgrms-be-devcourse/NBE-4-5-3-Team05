package com.NBE_4_5_2.Team5.domain.chat.service;


import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.repository.MessageRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class ChatRoomService {
//    private final ChatRoomRepository chatRoomRepository;

    // Redis CacheKeys
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
    public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;
    @Autowired
    private MessageRepository messageRepository;

    // 모든 채팅방 조회
    public List<ChatRoom> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    // 특정 채팅방 조회
    public ChatRoom findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    // 메세지 조회
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        return messageRepository.findAllByRoomId(roomId);
    }


    // 채팅방 생성
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = new ChatRoom(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);    // redis에 저장
        return chatRoom;
    }

    // 채팅방 삭제
    public void deleteChatRoom(String roomId) {
        hashOpsChatRoom.delete(CHAT_ROOMS, roomId);     // redis에서 삭제
    }

    // 유저가 입장한 채팅방ID와 유저 세션ID 맵핑 정보 저장
    public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    // 유저 세션으로 입장해 있는 채팅방 ID 조회
    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    // 유저 세션정보와 맵핑된 채팅방ID 삭제
    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }

    // 채팅방 유저수 조회
    public long getUserCount(String roomId) {
        return Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId)).orElse("0"));
    }

    // 채팅방에 입장한 유저수 +1
    public long plusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.increment(USER_COUNT + "_" + roomId)).orElse(0L);
    }

    // 채팅방에 입장한 유저수 -1
    public long minusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.decrement(USER_COUNT + "_" + roomId)).filter(count -> count > 0).orElse(0L);
    }

    // 현재 방에 참가중인 사용자 조회
    public List<String> getParticipants(String roomId) {
        List<String> participants = new ArrayList<>();

        // 모든 사용자의 세션 Id
        Map<String,String> entries = hashOpsEnterInfo.entries(ENTER_INFO);

        for(Map.Entry<String,String> entry:entries.entrySet()){
            if(entry.getValue().equals(roomId)){    // 탐색중인 방
                participants.add(entry.getKey());   // 참가자의 세션 ID
            }
        }
        return participants;
    }

    // 클라이언트별 개별 저장소 조회
    public List<ChatMessage> getMessagesByClientAndRoom(String client,String roomId) {
        return messageRepository.findAllByClientAndRoomId(client,roomId);
    }
}
