"use client";

import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/backend/client";
import { useEffect, useState } from "react";
import SockJS from 'sockjs-client'; 
import { Stomp } from "@stomp/stompjs"; 

export default function ClientPage({
  messages,
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
  const [chatMessages, setChatMessages] = useState<components["schemas"]["MessageDto"][]>(messages); 
  const [userNickname, setUserNickname] = useState<string>(""); 
  const [accessToken, setAccessToken] = useState<string>("");
  const [stompClient, setStompClient] = useState<any>(null);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const response = await client.GET('/api/chat/user', {
          headers: { cookie },
          credentials: "include"
        });
        
        if (response.data?.code === "200") {
          setUserNickname(response.data.data.name || "Unknown");
          setAccessToken(response.data.data.token || "");

          const socket = new SockJS("http://localhost:8080/ws-stomp");
          const client = Stomp.over(socket);
          setStompClient(client);

          console.log("연결 시도 중...");
          client.connect({ token: response.data.data.token }, (frame: string) => {
            console.log("연결 완료!", frame);
  
            client.subscribe(`/sub/chat/room/${roomId}`, (message: { body: string }) => {
              const messageData = JSON.parse(message.body);
              console.log("수신된 메시지: ", messageData); // 받은 메시지 로그

              // 현재 채팅방에 수신된 메시지만 업데이트 (중복되지 않도록)
              setChatMessages(prevMessages => 
                prevMessages.find(msg => msg.messageId === messageData.messageId) ? prevMessages : [...prevMessages, messageData]
              );
            });
          }, (error: any) => {
            alert("WebSocket 연결이 실패했습니다. 다시 시도해 주세요.");
          });
        }
      } catch (error) {
        console.error("사용자 정보 요청 실패:", error);
      }
    };

    fetchUserInfo();

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
      type: "TALK", // 메시지 타입 추가
    };

    stompClient.send("/pub/chat/message", { token: accessToken }, JSON.stringify(message));
    setInputMessage(""); // 입력창 초기화
    console.log("전송된 메시지: ", message);
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