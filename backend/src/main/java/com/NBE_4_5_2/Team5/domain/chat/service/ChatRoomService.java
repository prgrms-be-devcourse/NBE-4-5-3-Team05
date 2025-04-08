package com.NBE_4_5_2.Team5.domain.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.repository.ChatMessageRepository;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class ChatRoomService {

	// Redis CacheKeys
	private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
	public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
	public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장

	@Resource(name = "objectRedisTemplate")
	private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
	@Resource(name = "objectRedisTemplate")
	private HashOperations<String, String, String> hashOpsEnterInfo;
	@Resource(name = "objectRedisTemplate")
	private ValueOperations<String, String> valueOps;
	@Autowired
	private ChatMessageRepository chatMessageRepository;

	// 모든 채팅방 조회
	public List<ChatRoom> findAllRoom() {
		return hashOpsChatRoom.values(CHAT_ROOMS);
	}

	// roomId로 조회
	public ChatRoom findByRoomId(String roomId) {
		if (roomId == null){
			throw new ServiceException("404", "roomId가 null");
		}
		ChatRoom chatRoom = hashOpsChatRoom.get(CHAT_ROOMS, roomId);
		if(chatRoom == null){
			throw new ServiceException("404", "존재하지 않는 채팅방");
		}
		return chatRoom;
	}

	// 채팅방 반환(검증 포함)
	public ChatRoom getRoomByRoomId(String roomId,String username) {
		ChatRoom chatRoom = findByRoomId(roomId);
		if(!canAccess(roomId, username)) {
			throw new ForbiddenAccessException("405", "접근 권한 없는 채팅방");
		}

		if(getDeleteStatus(roomId, username)){
			throw new ServiceException("404","존재하지 않는 채팅방");
		}

		return chatRoom;
	}

	// 채팅방 생성
	public ChatRoom createChatRoom(String sender, String receiver) {
		String roomId = findByRoomIdByUsers(sender, receiver);
		System.out.println("roomId = " + roomId);
		// 방이 이미 존재
		if (roomId != null) {
			ChatRoom chatRoom = getRoomByRoomId(roomId,receiver);
			// 삭제 취소
			chatRoom.setDeleteStatus(sender,false);
			hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom);  // redis에 업데이트
			return chatRoom;
		}
		else {
			// 새로운 roomId 할당
			roomId = UUID.randomUUID().toString();
			ChatRoom chatRoom = new ChatRoom(sender, receiver);
			chatRoom.setRoomId(roomId);
			hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom);  // redis에 저장(sender)

			// 채팅방에 참가하는 유저의 세션 ID와 방 ID 매핑을 저장
			setUserEnterInfo(sender, roomId + "_" + sender); // 발신자 추가
			setUserEnterInfo(receiver, roomId + "_" + receiver); // 수신자 추가

			return chatRoom;
		}
	}

	// 접근 검증
	public boolean canAccess(String roomId, String username) {
		ChatRoom chatRoom = findByRoomId(roomId);
		if (chatRoom.getRoomId().equals(roomId)) {
            return chatRoom.getSender().equals(username) || chatRoom.getReceiver().equals(username); // 접근 허용
		}
		return false; // 접근 불가
	}

	// 논리적 삭제 여부 검증
	public boolean getDeleteStatus(String roomId, String username) {
		ChatRoom chatRoom = findByRoomId(roomId);
        return chatRoom.getDeleteStatus(username);
	}

	// 참가한 채팅방 목록 조회
	public List<ChatRoom> findRoomByUser(String username) {
		List<ChatRoom> chatRooms = new ArrayList<>();

		for (ChatRoom chatRoom : findAllRoom()) {
			if (chatRoom.getSender().equals(username) || chatRoom.getReceiver().equals(username)) {
				// 삭제된 채팅방 pass
				if(chatRoom.getDeleteStatus(username)) continue;
				chatRooms.add(chatRoom);
			}
		}

		if(chatRooms.isEmpty()){
			throw new ServiceException("404","존재하지 않는 채팅방");
		}

		return chatRooms;
	}

	// 메세지 조회
	public List<ChatMessage> getMessagesByUser(String roomId, String username) {
		if (!canAccess(roomId, username)) {
			throw new ForbiddenAccessException("405", "접근 권한 없는 채팅방");
		}

		List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId)
				.stream()
				// 삭제되지 않은 메세지만
				.filter(message -> !message.getDeleteStatus(username))
				.collect(Collectors.toList());

		return messages;
	}

	public void deleteMessage(String roomId, String username) {
		List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);
		// 삭제
		messages.forEach(message -> {
			message.setDeleteStatus(username, true);
		});
	}

	// 채팅방 삭제
	public void deleteChatRoom(String roomId, String username) {
		if(!canAccess(roomId, username)){
			throw new ForbiddenAccessException("405","접근 권한 없는 채팅방");
		}
		ChatRoom chatRoom = findByRoomId(roomId);
		chatRoom.setDeleteStatus(username,true);	// 논리적 삭제
		hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom);	// redis에 업데이트
		deleteMessage(roomId,username);	// 메세지 삭제
		// 양측에서 모두 삭제했을 경우
		if(isAllDelete(roomId)) {
			hashOpsChatRoom.delete(CHAT_ROOMS, roomId);     // redis에서 삭제
			System.out.println("완전 삭제");
		}
	}

	// 양측에서 삭제됐는지 검증
	public Boolean isAllDelete(String roomId){
		ChatRoom chatRoom = findByRoomId(roomId);
		String user1 = chatRoom.getSender();
		String user2 = chatRoom.getReceiver();

        return getDeleteStatus(roomId, user1) && getDeleteStatus(roomId, user2);
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
	public String findOther(String roomId, String username) {
		ChatRoom chatRoom = findByRoomId(roomId);
		if (username.equals(chatRoom.getSender())) {
			return chatRoom.getReceiver();
		} else if (username.equals(chatRoom.getReceiver())) {
			return chatRoom.getSender();
		}
		return null;
	}

	// 현재 두 사용자가 사용중인 roomId
	public String findByRoomIdByUsers(String sender, String receiver) {
		for (String key : hashOpsEnterInfo.keys(CHAT_ROOMS)) {
			ChatRoom chatRoom = hashOpsChatRoom.get(CHAT_ROOMS, key);

			if (chatRoom == null) {
				continue;
			}

			if (chatRoom.getSender().equals(sender) && chatRoom.getReceiver().equals(receiver)
				|| chatRoom.getSender().equals(receiver) && chatRoom.getReceiver().equals(sender)) {
				return chatRoom.getRoomId();
			}
		}
		return null;
	}

	public ChatRoom findRoomByClients(String sender, String receiver) {
		List<ChatRoom> chatRoomList = findRoomByUser(sender);
		for (ChatRoom chatRoom : chatRoomList) {
			if(chatRoom.getSender().equals(receiver) || chatRoom.getReceiver().equals(receiver)){
				// 삭제된 방 pass
				if(chatRoom.getDeleteStatus(sender)) continue;
				return chatRoom;
			}
		}
		throw new ServiceException("404","존재하지 않는 채팅방");
	}

}