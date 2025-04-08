package com.NBE_4_5_2.Team5.domain.chat.controller

import com.NBE_4_5_2.Team5.domain.chat.service.ChatRoomService
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatRoomControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var productPostRepository: ProductPostRepository

    @Autowired
    private lateinit var chatRoomService: ChatRoomService

    private lateinit var postId: String
    private lateinit var sender: String
    private lateinit var receiver: String
    private lateinit var token: String
    private lateinit var loginedUser: User
    private lateinit var roomId: String

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        setUpUserAndPost()
        roomId = setUpChatRoom()
    }

    @DisplayName("셋업_ 게시글,유저")
    fun setUpUserAndPost() {
        // 로그인 유저 설정
        loginedUser = userService.getUserByUsername("user3").orElseThrow {
            RuntimeException(
                "User not found"
            )
        }
        sender = loginedUser.nickname
        token = userService.generateAuthTokenAsString(loginedUser)
        println("토큰1: $token")

        // 포스트 ID 설정
        val post = productPostRepository.findAll().stream()
            .findFirst()
            .orElseThrow { RuntimeException("No posts found") }
        postId = post.id
        receiver = post.writer.username
    }

    @DisplayName("셋업_ 채팅방")
    @Throws(Exception::class)
    fun setUpChatRoom(): String {
        val action = mvc.perform(
            MockMvcRequestBuilders.post("/api/chat/room")
                .param("postId", postId) // 쿼리 파라미터
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print()) // 요청의 Content-Type
        roomId = JsonPath.read(action.andReturn().response.contentAsString, "$.data.roomId")
        println("sender: $sender")
        println("roomId: $roomId")
        return roomId
    }

    //    @AfterEach
    @DisplayName("초기화_ 채팅방 전체 비우기")
    @Throws(Exception::class)
    fun deleteAll() {
        val chatRoomList = chatRoomService.findRoomByUser(sender)

        for (chatRoom in chatRoomList) {
            roomId = chatRoom.roomId
            mvc.perform(
                MockMvcRequestBuilders.delete("/api/chat/message")
                    .param("roomId", roomId) // 삭제할 채팅방 ID
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Test
    @DisplayName("채팅방 생성")
    @Throws(Exception::class)
    fun createRoom() {
        //Given
        deleteAll() // 채팅방 비우기

        // When: 채팅방 생성 요청
        val action = mvc.perform(
            MockMvcRequestBuilders.post("/api/chat/room")
                .param("postId", postId) // 쿼리 파라미터
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print()) // 요청의 Content-Type

        // Then
        action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(Matchers.containsString(receiver)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.sender").value(sender))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.receiver").value(receiver))

        println("sender: $sender")
        println("receiver: $receiver")
        roomId = JsonPath.read(action.andReturn().response.contentAsString, "$.data.roomId")
        println("roomId: $roomId")
        println("채팅방 생성테스트 완료")
    }

    @Throws(Exception::class)
    @Test
    @DisplayName("채팅방 조회")
    fun testChatRooms() {
        // When: 채팅방 조회 요청
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // Then
        action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("채팅방 목록"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty()) // 데이터가 비어 있지 않음을 검증
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].other").value(receiver))

        roomId = JsonPath.read(
            action.andReturn().response.contentAsString,
            "$.data[0].roomId"
        )
        val other = JsonPath.read<String>(
            action.andReturn().response.contentAsString,
            "$.data[0].other"
        )
        println("roomId: $roomId")
        println("other: $other")
        println("receiver: $receiver")
    }

    @Throws(Exception::class)
    @Test
    @DisplayName("존재하지 않는 채팅방 조회")
    fun testNotExitChatRoom() {
        // Given
        deleteAll()

        // When: 채팅방 조회 요청
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // Then
        action.andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("404"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 채팅방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }


    @Test
    @DisplayName("채팅방 삭제")
    @Throws(Exception::class)
    fun deleteRoom() {
        // Given
        val chatRoomList = chatRoomService.findRoomByUser(sender)
        roomId = chatRoomList[0].roomId

        // When
        val action = mvc.perform(
            MockMvcRequestBuilders.delete("/api/chat/message")
                .param("roomId", roomId) // 삭제할 채팅방 ID
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        val getAction = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())


        // Then
        // 삭제 검증
        action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("삭제 완료"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())

        // 조회 검증
        getAction.andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("404"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 채팅방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty()) // 데이터가 비어 있음을 검증

        // roomId가 존재하지 않음을 검증
        val roomIds = JsonPath.read<List<String>>(getAction.andReturn().response.contentAsString, "$.data[*].roomId")
        Assertions.assertThat(roomIds).doesNotContain(roomId)
    }

    @Test
    @DisplayName("특정 사용자와의 채팅방 검색")
    @Throws(Exception::class)
    fun findChatRooms() {
        // Given
        createRoom()
        val chatRoomList = chatRoomService.findRoomByUser(sender)
        roomId = chatRoomList[0].roomId
        receiver = chatRoomList[0].getReceiver()
        println("receiver1: $receiver")

        // When: 검색 요청
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/search")
                .param("receiver", receiver)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        // Then: 검색 요청 결과 검증
        action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.roomId").value(roomId)) // 정확한 roomId를 반환하는지 검증
        println("receiver: $receiver")
        println("roomId: $roomId")
        deleteAll() // 초기화
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 검색")
    @Throws(Exception::class)
    fun findNotExistChatRoom() {
        // Given
        createRoom()
        val chatRoomList = chatRoomService.findRoomByUser(sender)
        roomId = chatRoomList[0].roomId
        receiver = chatRoomList[0].getReceiver()
        println("roomId: $roomId")
        println("receiver: $receiver")
        deleteAll() // 채팅방 삭제

        // When: 검색 요청
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/search")
                .param("receiver", receiver)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        // Then: 검색 요청 결과 검증
        action.andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("404"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 채팅방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }

    @Throws(Exception::class)
    @Test
    @DisplayName("roomId로 채팅방 검색")
    fun testRoomByRoomId() {
        // Given
        roomId = setUpChatRoom()
        println("검색전, roomId: $roomId")
        // When
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/room/$roomId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // Then
        action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("채팅방 반환"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.roomId").value(roomId))
    }

    @Throws(Exception::class)
    @Test
    @DisplayName("roomId로 채팅방 검색_실패")
    fun testNotExistRoomByRoomId() {
        // Given
        roomId = setUpChatRoom()
        deleteAll()
        // When
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/room/$roomId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // Then
        action.andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("404"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 채팅방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }

    @Throws(Exception::class)
    @Test
    @DisplayName("채팅방 메세지 조회")
    fun testMessages() {
        // Given
        roomId = setUpChatRoom()

        // When
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/message")
                .param("roomId", roomId)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(MockMvcResultHandlers.print())

        // Then
        action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("${receiver}와의 대화방"))
        // todo: 메세지까지 조회
    }


    @Test
    @DisplayName("접근 권한 없는 채팅방 메세지 조회")
    @Throws(Exception::class)
    fun CantAccessGetMessages() {
        // Given
        roomId = setUpChatRoom() // 채팅방 생성
        // 새로운 계정으로 로그인
        val loginedUser2 = userService.getUserByUsername("user2").orElseThrow {
            RuntimeException(
                "User not found"
            )
        }
        val token2 = userService.generateAuthTokenAsString(loginedUser2)
        println("토큰: $token2")

        // When
        val action = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/message")
                .param("roomId", roomId)
                .header("Authorization", "Bearer $token2")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        // Then
        action.andExpect(MockMvcResultMatchers.status().isMethodNotAllowed())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("405"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("접근 권한 없는 채팅방"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
    }

    @Test
    @DisplayName("고객센터 연결")
    @Throws(Exception::class)
    fun createCustomerService() {
        // Given
        deleteAll()
        // When
        val action = mvc.perform(
            MockMvcRequestBuilders.post("/api/chat/admin")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
        // 채팅방 조회
        val getAction = mvc.perform(
            MockMvcRequestBuilders.get("/api/chat/rooms")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())

        // Then
        action.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("고객센터"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty())
        // 조회 검증
        getAction.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("채팅방 목록"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty())

        // 관리자 검증
        val receiver = JsonPath.read<String>(action.andReturn().response.contentAsString, "$.data.receiver")
        val admin = userService.getUserByUsername(receiver).orElseThrow {
            RuntimeException(
                "User not found"
            )
        }
        org.junit.jupiter.api.Assertions.assertEquals(Role.ADMIN, admin.role)
    } // Given
    // When
    // Then
    //
    //
    //
    //
}