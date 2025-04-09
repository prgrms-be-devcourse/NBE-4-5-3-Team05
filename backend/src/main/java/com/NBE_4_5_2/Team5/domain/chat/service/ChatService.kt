package com.NBE_4_5_2.Team5.domain.chat.service

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom
import com.NBE_4_5_2.Team5.domain.chat.repository.ChatMessageRepository
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import jakarta.annotation.Resource
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val channelTopic: ChannelTopic,
    private val objectRedisTemplate: RedisTemplate<String, Any>,
    private val chatRoomService: ChatRoomService,
    private val chatMessageRepository: ChatMessageRepository,
    private val productPostService: ProductPostService,
) {

    @Resource(name = "objectRedisTemplate")
    private lateinit var hashOpsChatRoom: HashOperations<String, String, ChatRoom>

    companion object {
        private const val CHAT_ROOMS = "CHAT_ROOM"
    }
    /**
     * destination정보에서 roomId 추출
     */
    fun getRoomId(destination: String): String {
        val lastIndex = destination.lastIndexOf('/')
        return if (lastIndex != -1) destination.substring(lastIndex + 1)
        else ""
    }

    /**
     * 채팅방에 메시지 발송
     */
    @Transactional
    fun sendChatMessage(chatMessage: ChatMessage) {
        chatMessage.setUserCount(chatRoomService.getUserCount(chatMessage.getRoomId()))
        val chatRoom = chatRoomService.findBy_roomId(chatMessage.getRoomId())
        val receiver = chatRoomService.findOther(chatRoom.roomId, chatMessage.getSender())

        // 수신자가 채팅방 삭제한 상태
        if (chatRoom.getDeleteStatus(receiver)) {
            // 삭제 취소
            chatRoom.setDeleteStatus(receiver, false)
            hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.roomId, chatRoom) // 레디스 업데이트
        }

        if (ChatMessage.MessageType.TALK == chatMessage.getType()) {
            // redis로 메세지 발송
            objectRedisTemplate.convertAndSend(channelTopic.topic, chatMessage)
            val message = ChatMessage(
                ChatMessage.MessageType.TALK,
                chatRoom.roomId,
                chatMessage.getSender(),
                receiver,
                chatMessage.getMessage(),
                chatMessage.getImage(),
                chatMessage.getUserCount(),
                0.0f,
                0.0f
            )
            chatRoom.lastMessage = message.getMessage() // 마지막 메시지 업데이트
            chatRoom.lastTimestamp = message.getTimestamp() // 마지막 타임스탬프 업데이트

            hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.roomId, chatRoom) // 레디스에 업데이트
            // DB에 저장
            chatMessageRepository.save(message)
        } else if (ChatMessage.MessageType.IMAGE == chatMessage.getType()) {
            chatMessage.setMessage("")

            val message = ChatMessage(
                ChatMessage.MessageType.IMAGE,
                chatRoom.roomId,
                chatMessage.getSender(),
                receiver,
                "",  //					chatMessage.getMessage(),
                chatMessage.getImage(),
                chatMessage.getUserCount(),
                0.0f,
                0.0f
            )

            // redis로 메세지 발송
            objectRedisTemplate.convertAndSend(channelTopic.topic, message)
            hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.roomId, chatRoom) // 레디스에 업데이트
            chatMessageRepository.save(message)
        } else if (ChatMessage.MessageType.LOCATION == chatMessage.getType()) {
            // LOCATION 타입 처리를 위한 코드 추가
            val message = ChatMessage(
                ChatMessage.MessageType.LOCATION,
                chatRoom.roomId,
                chatMessage.getSender(),
                receiver,
                "",  //					chatMessage.getMessage(),
                chatMessage.getImage(),
                chatMessage.getUserCount(),
                chatMessage.getLatitude(),
                chatMessage.getLongitude()
            )
            // Redis로 메시지 발송
            objectRedisTemplate.convertAndSend(channelTopic.topic, message)
            hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.roomId, chatRoom) // 레디스에 업데이트
            chatMessageRepository.save(message)

        } else if (ChatMessage.MessageType.STATUS == chatMessage.getType()) {
            val writer = chatRoom.writer
            val sender = chatMessage.getSender()
            val postId = chatRoom.getPostId()

            // 메세지 전송자와 게시글 작성자가 다르면 권한x
            if(writer != sender){
                throw ServiceException("405", "권한이 없습니다.")
            }

            val message = ChatMessage(
                ChatMessage.MessageType.STATUS,
                chatRoom.roomId,
                chatMessage.getSender(),
                receiver,
                "",
                "",
                chatMessage.getUserCount(),
                0.0f,
                0.0f,
                chatMessage.getProductStatus()
            )

            // Redis로 메시지 발송
            objectRedisTemplate.convertAndSend(channelTopic.topic, message)
            hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.roomId, chatRoom) // 레디스에 업데이트
            chatMessageRepository.save(message)
            // 게시글 상태 변경
            productPostService.updateStatus(chatMessage.getProductStatus(), postId!!)
        }
    }
}