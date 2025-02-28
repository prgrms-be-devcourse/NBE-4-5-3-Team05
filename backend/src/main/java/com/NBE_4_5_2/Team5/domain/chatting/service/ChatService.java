package com.NBE_4_5_2.Team5.domain.chatting.service;

import com.NBE_4_5_2.Team5.domain.chatting.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chatting.repository.ChatRoomRespository;
import com.NBE_4_5_2.Team5.domain.user.entity.Users;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRoomRespository chatRoomRespository;
    private final UserRepository userRepository;

    public ChatRoom createRoom(Users sender, Users receiver) {

        ChatRoom chatRoom = ChatRoom.builder()
                .sender(sender)
                .receiver(receiver)
                .build();
        return chatRoomRespository.save(chatRoom);
    }


    public ChatRoom findById(String roomId) {
        Optional<ChatRoom> chatRoom=chatRoomRespository.findById(roomId);
        return chatRoom.orElse(null);
    }
}
