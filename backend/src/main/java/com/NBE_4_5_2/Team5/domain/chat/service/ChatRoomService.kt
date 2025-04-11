package com.NBE_4_5_2.Team5.domain.chat.service

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom
import com.NBE_4_5_2.Team5.domain.chat.repository.ChatMessageRepository
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException
import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
@Transactional
class ChatRoomService {
    companion object {
        private const val CHAT_ROOMS = "CHAT_ROOM" // 채팅룸 저장
        const val USER_COUNT: String = "USER_COUNT" // 채팅룸에 입장한 클라이언트수 저장
        const val ENTER_INFO: String = "ENTER_INFO" // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장
    }

    @Resource(name = "objectRedisTemplate")
    private lateinit var hashOpsChatRoom: HashOperations<String, String, ChatRoom>

    @Resource(name = "objectRedisTemplate")
    private lateinit var hashOpsEnterInfo: HashOperations<String, String, String>

    @Resource(name = "objectRedisTemplate")
    private lateinit var valueOps: ValueOperations<String, String>

    @Autowired
    private lateinit var chatMessageRepository: ChatMessageRepository

    // 모든 채팅방 조회
    fun findAllRoom(): List<ChatRoom> {
        return hashOpsChatRoom.values(CHAT_ROOMS)
    }

    // roomId로 조회
    fun findBy_roomId(roomId: String): ChatRoom {
        val chatRoom = hashOpsChatRoom[CHAT_ROOMS, roomId]
            ?: throw ServiceException("404", "존재하지 않는 채팅방")
        return chatRoom
    }

    // 채팅방 반환(검증 포함)
    fun getRoomByRoomId(roomId: String, username: String): ChatRoom {
        val chatRoom = findBy_roomId(roomId)
        if (!canAccess(roomId, username)) {
            throw ForbiddenAccessException("405", "접근 권한 없는 채팅방")
        }

        if (getDeleteStatus(roomId, username)) {
            throw ServiceException("404", "존재하지 않는 채팅방")
        }

        return chatRoom
    }

    // 채팅방 생성
    fun createChatRoom(sender: String, receiver: String, postId: String?,writer: String?): ChatRoom {
        var roomId = findByRoomIdByUsers(sender, receiver)
        // 방이 이미 존재
        if (roomId != null) {
            val chatRoom = getRoomByRoomId(roomId, receiver)
            // 삭제 취소
            chatRoom.setDeleteStatus(sender, false)
            hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom) // redis에 업데이트
            return chatRoom
        } else {
            // 새로운 roomId 할당
            roomId = UUID.randomUUID().toString()
            val chatRoom = ChatRoom(sender, receiver,postId)
            chatRoom.roomId = roomId
            chatRoom.writer = writer
            hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom) // redis에 저장(sender)

            // 채팅방에 참가하는 유저의 세션 ID와 방 ID 매핑을 저장
            setUserEnterInfo(sender, roomId + "_" + sender) // 발신자 추가
            setUserEnterInfo(receiver, roomId + "_" + receiver) // 수신자 추가

            return chatRoom
        }
    }

    // 접근 검증
    fun canAccess(roomId: String, username: String?): Boolean {
        val chatRoom = findBy_roomId(roomId)
        if (chatRoom.roomId == roomId) {
            return chatRoom.getSender() == username || chatRoom.getReceiver() == username // 접근 허용
        }
        return false // 접근 불가
    }

    // 논리적 삭제 여부 검증
    fun getDeleteStatus(roomId: String, username: String): Boolean {
        val chatRoom = findBy_roomId(roomId)
        return chatRoom.getDeleteStatus(username)
    }

    // 참가한 채팅방 목록 조회
    fun findRoomByUser(username: String?): List<ChatRoom> {
        val chatRooms: MutableList<ChatRoom> = ArrayList()

        for (chatRoom in findAllRoom()) {
            if (chatRoom.getSender() == username || chatRoom.getReceiver() == username) {
                // 삭제된 채팅방 pass
                if (chatRoom.getDeleteStatus(username)) continue
                chatRooms.add(chatRoom)
            }
        }

        if (chatRooms.isEmpty()) {
            throw ServiceException("404", "존재하지 않는 채팅방")
        }

        return chatRooms
    }

    // 메세지 조회
    fun getMessagesByUser(roomId: String, username: String?): List<ChatMessage> {
        if (!canAccess(roomId, username)) {
            throw ForbiddenAccessException("405", "접근 권한 없는 채팅방")
        }

        val messages = chatMessageRepository.findBy_roomId(roomId)
            .stream() // 삭제되지 않은 메세지만
            .filter { message: ChatMessage -> !message.getDeleteStatus(username)!! }
            .collect(Collectors.toList())

        return messages
    }

    fun deleteMessage(roomId: String, username: String) {
        val messages = chatMessageRepository.findBy_roomId(roomId)
        // 삭제
        messages.forEach(Consumer { message: ChatMessage ->
            message.setDeleteStatus(username, true)
        })
    }

    // 채팅방 삭제
    fun deleteChatRoom(roomId: String, username: String) {
        if (!canAccess(roomId, username)) {
            throw ForbiddenAccessException("405", "접근 권한 없는 채팅방")
        }

        val chatRoom = findBy_roomId(roomId)
        chatRoom.setDeleteStatus(username, true) // 논리적 삭제
        hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom) // redis에 업데이트
        deleteMessage(roomId, username) // 메세지 삭제
        // 양측에서 모두 삭제했을 경우
        if (isAllDelete(roomId)) {
            hashOpsChatRoom.delete(CHAT_ROOMS, roomId) // redis에서 삭제
        }
    }

    // 양측에서 삭제됐는지 검증
    fun isAllDelete(roomId: String): Boolean {
        val chatRoom = findBy_roomId(roomId)
        val user1 = chatRoom.getSender()
        val user2 = chatRoom.getReceiver()

        return getDeleteStatus(roomId, user1) && getDeleteStatus(roomId, user2)
    }

    // 유저가 입장한 채팅방ID와 유저 세션ID 맵핑 정보 저장
    fun setUserEnterInfo(sessionId: String, roomId: String) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId)
    }

    // 유저 세션으로 입장해 있는 채팅방 ID 조회
    fun getUserEnterRoomId(sessionId: String): String? {
        return hashOpsEnterInfo[ENTER_INFO, sessionId]
    }

    // 유저 세션정보와 맵핑된 채팅방ID 삭제
    fun removeUserEnterInfo(sessionId: String) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId)
    }

    // 채팅방 유저수 조회
    fun getUserCount(roomId: String): Long {
        return Optional.ofNullable(valueOps[USER_COUNT + "_" + roomId]).orElse("0").toLong()
    }

    // 채팅방에 입장한 유저수 +1
    fun plusUserCount(roomId: String): Long {
        return Optional.ofNullable(valueOps.increment(USER_COUNT + "_" + roomId)).orElse(0L)
    }

    // 채팅방에 입장한 유저수 -1
    fun minusUserCount(roomId: String?): Long {
        return Optional.ofNullable(valueOps.decrement(USER_COUNT + "_" + roomId)).filter { count: Long -> count > 0 }
            .orElse(0L)
    }

    // 현재 방에 참가중인 사용자 조회
    fun findOther(roomId: String, username: String?): String {
        val chatRoom = findBy_roomId(roomId)
        if (username == chatRoom.getSender()) {
            return chatRoom.getReceiver()
        } else if (username == chatRoom.getReceiver()) {
            return chatRoom.getSender()
        }
        throw ServiceException("404", "존재하지 않는 사용자")
    }

    // 현재 두 사용자가 사용중인 roomId
    fun findByRoomIdByUsers(sender: String, receiver: String): String? {
        for (key in hashOpsEnterInfo.keys(CHAT_ROOMS)) {
            val chatRoom = hashOpsChatRoom[CHAT_ROOMS, key] ?: continue

            if (chatRoom.getSender() == sender && chatRoom.getReceiver() == receiver
                || chatRoom.getSender() == receiver && chatRoom.getReceiver() == sender
            ) {
                return chatRoom.roomId
            }
        }
        return null
    }

    fun findRoomByClients(sender: String, receiver: String): ChatRoom {
        val chatRoomList = findRoomByUser(sender)
        for (chatRoom in chatRoomList) {
            if (chatRoom.getSender() == receiver || chatRoom.getReceiver() == receiver) {
                // 삭제된 방 pass
                if (chatRoom.getDeleteStatus(sender)) continue
                return chatRoom
            }
        }
        throw ServiceException("404", "존재하지 않는 채팅방")
    }

}