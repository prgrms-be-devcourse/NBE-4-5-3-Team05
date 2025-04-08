package com.NBE_4_5_2.Team5.domain.chat.controller;

import com.NBE_4_5_2.Team5.domain.chat.dto.ChatRoomDto;
import com.NBE_4_5_2.Team5.domain.chat.dto.MessageDto;
import com.NBE_4_5_2.Team5.domain.chat.entity.AccessProvider;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage;
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom;
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.user.service.AuthTokenService;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.Rq;
import com.NBE_4_5_2.Team5.global.dto.Empty;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException;
import com.NBE_4_5_2.Team5.global.exception.security.WrongRoleException;
import com.NBE_4_5_2.Team5.global.exception.user.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/chat")
@Tag(name = "Chat Room API", description = "채팅방 관리 API")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;
	private final AuthTokenService authTokenService;
	private final ProductPostService productPostService;
	private final Rq rq;
	private final UserService userService;
	private final UserRepository userRepository;
	private final UserAuthService userAuthService;

	// 사용자 토큰 조회
	@Operation(summary = "사용자 토큰 조회", description = "사용자를 판단하기 위한 토큰을 생성해 반환합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@GetMapping("/user")
	@ResponseBody
	public RsData<AccessProvider> getUserInfo() {
		String token = rq.getValueFromCookie("accessToken");
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);

		AccessProvider access = AccessProvider.builder()
				.name(user.getNickname())  // 사용자 이름
				.token(token)
				.build();

		return new RsData<>("200", "success", access);
	}

	// 채팅방 생성
	@Operation(summary = "채팅방 생성", description = "상품 판매자와의 채팅방을 생성합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@PostMapping("/room")
	@ResponseBody
	@Transactional
	public RsData<ChatRoom> createRoom(
			@Parameter(description = "상품 게시글 아이디", example = "ppost-fkkdsjf9adsa-ds8fdfsdf-289103yd")
			@RequestParam String postId) {
		User userIdentity = userAuthService.getUserIdentity();
		User sender = userAuthService.getRealActor(userIdentity);

		ProductPostResponse postResponse = productPostService.getPost(postId);
		String receiver = postResponse.getWriterName();
		ChatRoom chatRoom = chatRoomService.createChatRoom(sender.getNickname(), receiver);

		return new RsData<>("200", receiver + "와의 채팅방", chatRoom);
	}

	// 고객센터
	@Operation(summary = "관리자와의 채팅방 생성", description = "관리자와의 채팅방을 생성합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@PostMapping("/admin")
	@ResponseBody
	@Transactional
	public RsData<ChatRoom> createRoomAdmin(){
		User userIdentity = userAuthService.getUserIdentity();
		User sender = userAuthService.getRealActor(userIdentity);

		User admin = userService.getAdminUsers();
		String receiver = admin.getNickname();

		ChatRoom chatRoom = chatRoomService.createChatRoom(sender.getNickname(), receiver);

		return new RsData<>("200", "고객센터", chatRoom);
	}

	// 채팅방 조회
	@Operation(summary = "채팅방 조회", description = "유저가 속한 채팅방을 모두 조회합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@GetMapping("/rooms")
	@ResponseBody
	public RsData<List<ChatRoomDto>> getUserRooms() {
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);

		List<ChatRoom> chatRoomsList=chatRoomService.findRoomByUser(user.getNickname());
		List<ChatRoomDto> response=chatRoomsList.stream()
				.map(chatRoom -> {
					String other=chatRoomService.findOther(chatRoom.getRoomId(),user.getNickname());
					List<ChatMessage> messages= chatRoomService.getMessagesByUser(chatRoom.getRoomId(),user.getNickname());
					String lastMessage="";
					ChatMessage.MessageType messageType = ChatMessage.MessageType.TALK;	// 초기값
					String lastTimestamp="";
					if(!messages.isEmpty()) {
						lastMessage = messages.get(messages.size() - 1).getMessage();
						messageType = messages.get(messages.size() - 1).getType();
						lastTimestamp = messages.get(messages.size() - 1).getTimestamp();
					}
					return new ChatRoomDto(
							chatRoom.getId(),
							chatRoom.getRoomId(),
							chatRoom.getName(),
							chatRoom.getUserCount(),
							lastMessage,
							messageType,
							lastTimestamp,
							other
					);
				})
				.toList();
		return new RsData<>("200","채팅방 목록",response);
	}

	@GetMapping("/room/{roomId}")
	@ResponseBody
	public RsData<ChatRoom> getRoomByRoomId(@PathVariable String roomId) {
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);

		ChatRoom chatRoom=chatRoomService.getRoomByRoomId(roomId,user.getNickname());
		return new RsData<>("200","채팅방 반환",chatRoom);
    }

	// 채팅방 메세지 조회
	@Operation(summary = "채팅방 메세지 조회", description = "채팅방의 모든 메시지를 조회합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@GetMapping("/message")
	@ResponseBody
	public RsData<List<MessageDto>> getMessages(
			@Parameter(description = "채팅방 id", example = "123")
			@RequestParam String roomId) {
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);

		List<ChatMessage> messages= chatRoomService.getMessagesByUser(roomId,user.getNickname());
		String other=chatRoomService.findOther(roomId,user.getNickname());

		List<MessageDto> response=messages.stream()
				.map(chatMessage -> new MessageDto(
						chatMessage.getMessageId(),
						chatMessage.getSender(),
						chatMessage.getMessage(),
						chatMessage.getImage(),
						chatMessage.getLatitude(),
						chatMessage.getLongitude(),
						chatMessage.getTimestamp(),
						messages.get(messages.size()-1).getMessage(),
						messages.get(messages.size()-1).getTimestamp()))
				.toList();
		return new RsData<>("200",other+"와의 대화방",response);
	}

	// 채팅방 삭제
	@Operation(summary = "채팅방 삭제", description = "유저가 속한 채팅방을 id로 삭제합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@DeleteMapping("/message")
	@ResponseBody
	public RsData<?> deleteRoom(
			@Parameter(description = "채팅방 id", example = "123")
			@RequestParam String roomId) {
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);

		chatRoomService.deleteChatRoom(roomId, user.getNickname());
		return new RsData<>("200", "삭제 완료",new Empty());
	}

	// 특정 사용자와의 채팅방 검색
	@Operation(summary = "채팅방 검색", description = "특정 유저가 속한 채팅방을 검색합니다.")
	@PreAuthorize("isAuthenticated()")
	@SecurityRequirement(name = "cookieAuth")
	@GetMapping("/search")
	@ResponseBody
	public RsData<ChatRoomDto> findChatRooms(
			@Parameter(description = "찾으려는 채팅방에 속한 유저의 ID", example = "user-12k3j-sjdfi2jj-431iojr124io1")
			@RequestParam String receiver) {
		User userIdentity = userAuthService.getUserIdentity();
		User user = userAuthService.getRealActor(userIdentity);

		ChatRoom chatRoom=chatRoomService.findRoomByClients(user.getNickname(),receiver);
		List<ChatMessage> messages= chatRoomService.getMessagesByUser(chatRoom.getRoomId(),user.getNickname());
		String lastMessage="";
		ChatMessage.MessageType messageType = ChatMessage.MessageType.TALK;	// 초기값
		String lastTimestamp="";
		if(!messages.isEmpty()) {
			lastMessage = messages.get(messages.size() - 1).getMessage();
			messageType = messages.get(messages.size() - 1).getType();
			lastTimestamp = messages.get(messages.size() - 1).getTimestamp();
		}
		ChatRoomDto chatRoomDto = new ChatRoomDto(
				chatRoom.getId(),
				chatRoom.getRoomId(),
				chatRoom.getName(),
				chatRoom.getUserCount(),
				lastMessage, // 마지막 메시지 내용
				messageType, // 마지막 메세지의 타입
				lastTimestamp,
				receiver
		);
		return new RsData<>("200","success",chatRoomDto);
	}
}