"use client";

import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/backend/client"; // Axios 클라이언트
import { useEffect, useState } from "react";
import SockJS from 'sockjs-client'; 
import { Stomp } from "@stomp/stompjs"; 

export default function ClientPage({
  messages,  // 서버에서 전달된 초기 메시지 데이터
  title,
  roomId,
  cookie,
}: {
  messages: components["schemas"]["MessageDto"][];
  title: string;
  roomId: string;
  cookie: string;
}) {
  const [inputMessage, setInputMessage] = useState<string>(""); 
  const [chatMessages, setChatMessages] = useState<components["schemas"]["MessageDto"][]>(messages); // 초기 메시지로 설정
  const [userNickname, setUserNickname] = useState<string>(""); 
  const [accessToken, setAccessToken] = useState<string>("");
  const [stompClient, setStompClient] = useState<any>(null);

  useEffect(() => {
    const fetchUserInfoAndConnect = async () => {
      try {
        const userResponse = await client.GET('/api/chat/user', {
          headers: { cookie },
          credentials: "include"
        });

        if (userResponse.data?.code === "200") {
          setUserNickname(userResponse.data.data.name || "Unknown");
          setAccessToken(userResponse.data.data.token || "");

          const socket = new SockJS("http://localhost:8080/ws-stomp");
          const stompClient = Stomp.over(socket);
          setStompClient(stompClient);

          console.log("연결 시도 중...");
          stompClient.connect({ token: accessToken }, (frame:string) => {
            console.log("연결 완료!", frame);
          
            stompClient.subscribe(`/sub/chat/room/${roomId}`, async (message: { body: string }) => {
              const messageData = JSON.parse(message.body);
              console.log("수신된 메시지: ", messageData);
              
              // 서버에서 최신 메시지를 가져오기 위한 API 호출
              const messageResponse = await client.GET("/api/chat/message", {
                headers: { cookie },
                params: {
                  query: { roomId },
                },
                credentials: "include"
              });
              
              if (messageResponse.data?.code === "200") {
                setChatMessages(messageResponse.data.data); // 수신된 메시지로 상태 업데이트
              }
            });
          }, (error: any) => {
            alert("WebSocket 연결 실패했습니다. 다시 시도해 주세요.");
          });
        }
      } catch (error) {
        console.error("사용자 정보 요청 실패:", error);
      }
    };

    fetchUserInfoAndConnect();

    return () => {
      if (stompClient) {
        stompClient.disconnect();
        console.log("WebSocket 연결 종료");
      }
    };
  }, [roomId, cookie]);

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setInputMessage(event.target.value);
  };

  const handleSendMessage = () => {
    if (inputMessage.trim() === "") {
      alert("메시지를 입력하세요.");
      return;
    }
  
    if (!stompClient) {
      alert("WebSocket 연결이 되어 있지 않습니다.");
      return;
    }
  
    const message = {
      roomId,
      message: inputMessage,
      sender: userNickname,
      type: "TALK",
    };
  
    stompClient.send("/pub/chat/message", { token: accessToken }, JSON.stringify(message));
    setInputMessage(""); // 입력창 초기화
  };
  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      handleSendMessage();
    }
  };

  const deleteRoom = async () => {
    try {
      await client.DELETE(`/api/chat/message`, {
        headers: { cookie },
        params: { query: { roomId } },
        credentials: 'include',
      });
      alert("채팅방 삭제가 완료되었습니다.");
      window.location.href = "/chat"; 
    } catch (error) {
      console.error("채팅방 삭제 중 오류 발생:", error);
      alert("채팅방 삭제 중 오류가 발생했습니다.");
    }
  };
  const handleDelete = () => {
    if (window.confirm("메세지가 삭제됩니다. 정말로 나가시겠습니까? ")) {
      deleteRoom();
    }
  };

  return (
    <div>
      <h1 className="text-xl font-bold mb-4">{title}</h1>
      <button 
        onClick={handleDelete}
        className="mb-4 px-4 py-2 bg-red-600 text-white rounded"
      >
        채팅방 나가기
      </button>
      <ul className="space-y-4">
        {chatMessages.slice().reverse().map((message) => (
          <li key={message.messageId} className="border p-2 bg-white rounded shadow-sm">
            <div>
              <strong>보낸 이:</strong> {message.sender}
            </div>
            <div>
              <strong>메시지 내용:</strong> {message.message}
            </div>
            {message.image && (
              <div>
                <strong>이미지:</strong> <img src={message.image} alt="메시지 첨부 이미지" className="max-w-full h-auto" />
              </div>
            )}
            <div>
              <strong>보낸 시간:</strong> {message.timestamp} 
            </div>
          </li>
        ))}
      </ul>
      <div className="flex mt-4">
        <input
          type="text"
          value={inputMessage}
          onChange={handleInputChange}
          onKeyPress={handleKeyPress} 
          placeholder="메시지를 입력하세요."
          className="border p-2 flex-grow rounded"
        />
        <button
          onClick={handleSendMessage}
          className="ml-2 px-4 py-2 bg-blue-500 text-white rounded"
        >
          전송
        </button>
      </div>
    </div>
  );
}