package com.NBE_4_5_2.Team5.domain.chat.controller

import com.NBE_4_5_2.Team5.domain.chat.dto.ChatRoomDto
import com.NBE_4_5_2.Team5.domain.chat.dto.MessageDto
import com.NBE_4_5_2.Team5.domain.chat.entity.AccessProvider
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatMessage
import com.NBE_4_5_2.Team5.domain.chat.entity.ChatRoom
import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserAuthService
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.dto.Empty
import com.NBE_4_5_2.Team5.global.response.RsData
import com.NBE_4_5_2.Team5.global.rq.Rq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/chat")
@Tag(name = "Chat Room API", description = "채팅방 관리 API")
class ChatRoomController(

    private val chatRoomService: ChatRoomService,
    private val productPostService: ProductPostService,
    private val rq: Rq,
    private val userService: UserService,
    private val userAuthService: UserAuthService
) {

    @get:ResponseBody
    @get:GetMapping("/user")
    @get:SecurityRequirement(name = "cookieAuth")
    @get:PreAuthorize("isAuthenticated()")
    @get:Operation(summary = "사용자 토큰 조회", description = "사용자를 판단하기 위한 토큰을 생성해 반환합니다.")
    val userInfo: RsData<AccessProvider>
        // 사용자 토큰 조회
        get() {
            val token = rq.getValueFromCookie("accessToken")
            val userIdentity = userAuthService.userIdentity
            val user = userAuthService.getRealActor(userIdentity)

            val access = AccessProvider(
                user.nickname,  // 사용자 이름
                token!!
            )
            return RsData("200", "success", access)
        }

    // 채팅방 생성
    @Operation(summary = "채팅방 생성", description = "상품 판매자와의 채팅방을 생성합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @PostMapping("/room")
    @ResponseBody
    @Transactional
    fun createRoom(
        @Parameter(
            description = "상품 게시글 아이디",
            example = "ppost-fkkdsjf9adsa-ds8fdfsdf-289103yd"
        ) @RequestParam postId: String
    ): RsData<ChatRoom> {
        val userIdentity = userAuthService.userIdentity
        val sender = userAuthService.getRealActor(userIdentity)

        val postResponse = productPostService.getPost(postId)
        val writer = postResponse.writerId
        val receiver = postResponse.writerName
        val chatRoom = chatRoomService.createChatRoom(sender.nickname, receiver,postId,writer)

        return RsData("200", receiver + "와의 채팅방", chatRoom)
    }

    // 고객센터
    @Operation(summary = "관리자와의 채팅방 생성", description = "관리자와의 채팅방을 생성합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @PostMapping("/admin")
    @ResponseBody
    @Transactional
    fun createRoomAdmin(): RsData<ChatRoom> {
        val userIdentity = userAuthService.userIdentity
        val sender = userAuthService.getRealActor(userIdentity)

        val admin = userService.adminUsers
        val receiver = admin.nickname

        val chatRoom = chatRoomService.createChatRoom(sender.nickname, receiver,null,null)

        return RsData("200", "고객센터", chatRoom)
    }

    @get:ResponseBody
    @get:GetMapping("/rooms")
    @get:SecurityRequirement(name = "cookieAuth")
    @get:PreAuthorize("isAuthenticated()")
    @get:Operation(summary = "채팅방 조회", description = "유저가 속한 채팅방을 모두 조회합니다.")
    val userRooms: RsData<List<ChatRoomDto>>
        // 채팅방 조회
        get() {
            val userIdentity = userAuthService.userIdentity
            val user = userAuthService.getRealActor(userIdentity)

            val chatRoomsList = chatRoomService.findRoomByUser(user.nickname)
            val response = chatRoomsList.stream()
                .map { chatRoom: ChatRoom ->
                    val other = chatRoomService.findOther(chatRoom.roomId, user.nickname)
                    val messages =
                        chatRoomService.getMessagesByUser(chatRoom.roomId, user.nickname)
                    var lastMessage = ""
                    var messageType =
                        ChatMessage.MessageType.TALK // 초기값
                    var lastTimestamp = ""
                    if (!messages.isEmpty()) {
                        lastMessage = messages[messages.size - 1].getMessage()
                        messageType = messages[messages.size - 1].getType()
                        lastTimestamp = messages[messages.size - 1].getTimestamp()
                    }
                    ChatRoomDto(
                        chatRoom.id,
                        chatRoom.roomId,
                        chatRoom.getPostId(),
                        chatRoom.writer,
                        chatRoom.name,
                        chatRoom.userCount,
                        lastMessage,
                        messageType,
                        lastTimestamp,
                        other
                    )
                }
                .toList()
            return RsData(
                "200",
                "채팅방 목록",
                response
            )
        }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    fun getRoomByRoomId(@PathVariable roomId: String): RsData<ChatRoom> {
        val userIdentity = userAuthService.userIdentity
        val user = userAuthService.getRealActor(userIdentity)

        val chatRoom = chatRoomService.getRoomByRoomId(roomId, user.nickname)
        return RsData("200", "채팅방 반환", chatRoom)
    }

    // 채팅방 메세지 조회
    @Operation(summary = "채팅방 메세지 조회", description = "채팅방의 모든 메시지를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/message")
    @ResponseBody
    fun getMessages(
        @Parameter(description = "채팅방 id", example = "123") @RequestParam roomId: String
    ): RsData<List<MessageDto>> {
        val userIdentity = userAuthService.userIdentity
        val user = userAuthService.getRealActor(userIdentity)

        val messages = chatRoomService.getMessagesByUser(roomId, user.nickname)
        val other = chatRoomService.findOther(roomId, user.nickname)

        val response = messages.stream()
            .map { chatMessage: ChatMessage ->
                MessageDto(
                    chatMessage.getMessageId(),
                    chatMessage.getSender(),
                    chatMessage.getMessage(),
                    chatMessage.getImage(),
                    chatMessage.getLatitude(),
                    chatMessage.getLongitude(),
                    chatMessage.getTimestamp(),
                    messages[messages.size - 1].getMessage(),
                    messages[messages.size - 1].getTimestamp(),
                    messages[messages.size - 1].getType()
                )
            }
            .toList()
        return RsData("200", other + "와의 대화방", response)
    }

    // 채팅방 삭제
    @Operation(summary = "채팅방 삭제", description = "유저가 속한 채팅방을 id로 삭제합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @DeleteMapping("/message")
    @ResponseBody
    fun deleteRoom(
        @Parameter(description = "채팅방 id", example = "123") @RequestParam roomId: String
    ): RsData<*> {
        val userIdentity = userAuthService.userIdentity
        val user = userAuthService.getRealActor(userIdentity)

        chatRoomService.deleteChatRoom(roomId, user.nickname)
        return RsData("200", "삭제 완료", Empty())
    }

    // 특정 사용자와의 채팅방 검색
    @Operation(summary = "채팅방 검색", description = "특정 유저가 속한 채팅방을 검색합니다.")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @GetMapping("/search")
    @ResponseBody
    fun findChatRooms(
        @Parameter(
            description = "찾으려는 채팅방에 속한 유저의 ID",
            example = "user-12k3j-sjdfi2jj-431iojr124io1"
        ) @RequestParam receiver: String
    ): RsData<ChatRoomDto> {
        val userIdentity = userAuthService.userIdentity
        val user = userAuthService.getRealActor(userIdentity)

        val chatRoom = chatRoomService.findRoomByClients(user.nickname, receiver)
        val messages = chatRoomService.getMessagesByUser(chatRoom.roomId, user.nickname)
        var lastMessage = ""
        var messageType = ChatMessage.MessageType.TALK // 초기값
        var lastTimestamp = ""
        if (!messages.isEmpty()) {
            lastMessage = messages[messages.size - 1].getMessage()
            messageType = messages[messages.size - 1].getType()
            lastTimestamp = messages[messages.size - 1].getTimestamp()
        }
        val chatRoomDto = ChatRoomDto(
            chatRoom.id,
            chatRoom.roomId,
            chatRoom.getPostId(),
            chatRoom.writer,
            chatRoom.name,
            chatRoom.userCount,
            lastMessage,  // 마지막 메시지 내용
            messageType,  // 마지막 메세지의 타입
            lastTimestamp,
            receiver
        )
        return RsData("200", "success", chatRoomDto)
    }
}