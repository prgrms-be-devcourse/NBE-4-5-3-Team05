package com.NBE_4_5_2.Team5.domain.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.repository.MessageRepository;
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
	private MessageRepository messageRepository;
	@Autowired
	private ProductPostService productPostService;
	@Autowired
	private Rq rq;
	@Autowired
	private UserService userService;

	// 모든 채팅방 조회
	public List<ChatRoom> findAllRoom() {
		return hashOpsChatRoom.values(CHAT_ROOMS);
	}

	// roomId로 조회
	public List<ChatRoom> findByRoomId(String roomId) {
		if (roomId == null)
			return null;

		List<ChatRoom> chatRooms = new ArrayList<>();
		for (String key : hashOpsChatRoom.keys(CHAT_ROOMS)) {
			if (key.startsWith(roomId)) {
				ChatRoom chatRoom = hashOpsChatRoom.get(CHAT_ROOMS, key);
				if (chatRoom != null) {
					chatRooms.add(chatRoom);
				}
			}
		}
		return chatRooms;
	}

	// 채팅방 생성
	public ChatRoom createChatRoom(String sender, String receiver) {
		String roomId = findByRoomIdByUsers(sender, receiver);
		List<ChatRoom> chatRooms = findByRoomId(roomId);
		// 방이 이미 존재
		if (roomId != null && chatRooms.size() == 2) {
			return findChatRoomByClient(roomId, receiver);
		}
		if (roomId != null && chatRooms.size() == 1) {
			// 클라이언트
			String client = chatRooms.get(0).getClient();
			// 상대방
			String other = findOther(roomId, client);

			ChatRoom chatRoom1 = new ChatRoom(other, client);
			chatRoom1.setRoomId(roomId);
			chatRoom1.setClient(other);
			hashOpsChatRoom.put(CHAT_ROOMS, roomId + "_" + other, chatRoom1);  // redis에 저장(sender)

			setUserEnterInfo(sender, roomId + "_" + other); // 발신자 추가
			return chatRoom1;

		} else {
			// 새로운 roomId 할당
			roomId = UUID.randomUUID().toString();
			ChatRoom chatRoom1 = new ChatRoom(sender, receiver);
			chatRoom1.setRoomId(roomId);
			chatRoom1.setClient(sender);
			hashOpsChatRoom.put(CHAT_ROOMS, roomId + "_" + sender, chatRoom1);  // redis에 저장(sender)

			ChatRoom chatRoom2 = new ChatRoom(sender, receiver);
			chatRoom2.setRoomId(roomId);    // 동일한 roomId
			chatRoom2.setClient(receiver);
			hashOpsChatRoom.put(CHAT_ROOMS, roomId + "_" + receiver, chatRoom2);    // redis에 저장(receiver)

			// 채팅방에 참가하는 유저의 세션 ID와 방 ID 매핑을 저장
			setUserEnterInfo(sender, roomId + "_" + sender); // 발신자 추가
			setUserEnterInfo(receiver, roomId + "_" + receiver); // 수신자 추가

			return chatRoom1;
		}
	}

	// 접근 검증
	public boolean canAccess(String roomId, String username) {
		for (ChatRoom chatRoom : findByRoomId(roomId)) {
			if (chatRoom.getRoomId().equals(roomId) && chatRoom.getClient().equals(username)) {
				return true; // 접근 허용
			}
		}
		return false; // 접근 불가
	}

	// 참가한 채팅방 목록 조회
	public List<ChatRoom> findRoomByUser(String username) {
		List<ChatRoom> chatRooms = new ArrayList<>();

		for (ChatRoom chatRoom : findAllRoom()) {
			if (chatRoom.getClient().equals(username)) {
				chatRooms.add(chatRoom);
			}
		}
		return chatRooms;
	}

	// 개별저장소 탐색
	public ChatRoom findChatRoomByClient(String roomId, String username) {
		for (ChatRoom chatRoom : findByRoomId(roomId)) {
			if (chatRoom.getClient().equals(username)) {
				return chatRoom;
			}
		}
		return null;
	}

	// 메세지 조회
	public List<ChatMessage> getMessagesByUser(String roomId, String username) {

		if (!canAccess(roomId, username)) {
			throw new ForbiddenAccessException("404", "접근 권한 없는 채팅방");
		}
		ChatRoom chatRoom = findChatRoomByClient(roomId, username);

		return messageRepository.findAllByClientAndRoomId(chatRoom.getId(), roomId);
	}

	// 채팅방 삭제
	public void deleteChatRoom(String roomId, String username) {
		ChatRoom chatRoom = findChatRoomByClient(roomId, username);
		String client = chatRoom.getId();
		hashOpsChatRoom.delete(CHAT_ROOMS, roomId + "_" + username);     // redis에서 삭제
		messageRepository.deleteAllByClient(client);
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
		for (ChatRoom chatRoom : findByRoomId(roomId)) {
			if (username.equals(chatRoom.getSender())) {
				return chatRoom.getReceiver();
			} else if (username.equals(chatRoom.getReceiver())) {
				return chatRoom.getSender();
			}
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

	// 현재 두 사용자가 사용중인 roodId(개별저장소)
	public String findByRoomIdByClients(String sender, String receiver) {
		for (String key : hashOpsEnterInfo.keys(CHAT_ROOMS)) {
			ChatRoom chatRoom = hashOpsChatRoom.get(CHAT_ROOMS, key);

			if (chatRoom == null) {
				continue;
			}

			if (chatRoom.getSender().equals(sender) && chatRoom.getReceiver().equals(receiver)
				|| chatRoom.getSender().equals(receiver) && chatRoom.getReceiver().equals(sender)) {
				if (chatRoom.getClient().equals(sender)) {
					return chatRoom.getRoomId();
				}
			}
		}
		return null;
	}

}
