"use client";

import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/backend/client"; 
import { useEffect, useRef, useState } from "react";
import SockJS from 'sockjs-client'; 
import { Stomp } from "@stomp/stompjs"; 
import { headers } from "next/headers";

export default function ClientPage({
  messages,
  title,
  roomId,
  cookie,
  chatRoom,
}: {
  messages: components["schemas"]["MessageDto"][];
  title: string;
  roomId: string;
  cookie: string;
  chatRoom: components["schemas"]["ChatRoom"];
}) {
  const [inputMessage, setInputMessage] = useState<string>(""); 
  const [chatMessages, setChatMessages] = useState<components["schemas"]["MessageDto"][]>(messages); // 초기 메시지로 설정
  const [userNickname, setUserNickname] = useState<string>(""); 
  const [accessToken, setAccessToken] = useState<string>("");
  const [stompClient, setStompClient] = useState<any>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

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

  const handleSendLocation = () => {
    if (!stompClient) {
        alert("WebSocket 연결이 되어 있지 않습니다.");
        return;
    }

    navigator.geolocation.getCurrentPosition((position) => {
        const latitude = position.coords.latitude;
        const longitude = position.coords.longitude;

        const message = {
            roomId,
            sender: userNickname,
            type: "LOCATION",
            latitude, // 위도 
            longitude, // 경도
            messageId: null, // 필요 시 비워둡니다
            image: null, // 필요 시 비워둡니다
            timestamp: new Date().toISOString(), // 현재 시간 설정
        };

        stompClient.send("/pub/chat/message", { token: accessToken }, JSON.stringify(message));
        alert("위치가 전송되었습니다!");
    }, (error) => {
        console.error("Error obtaining location:", error);
        alert("위치를 가져오는 데 실패했습니다.");
    });
  };

  const handleFileSelect = () => {
    fileInputRef.current?.click(); // 파일 입력 클릭
  };
  
  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file); // 파일 추가

    try {
        // fetch를 사용하여 파일 업로드
        const response = await fetch("http://localhost:8080/api/uploadFile", {
            method: "POST",
            body: formData, 
        });

        if (!response.ok) {
            throw new Error("파일 업로드 실패");
        }

        const imageUrl = await response.text(); // 서버에서 반환된 이미지 URL을 가져옵니다.
        console.log("서버에서 반환된 이미지 URL:", imageUrl);

        const message = {
            roomId,
            type: "IMAGE",
            sender: userNickname,
            image: imageUrl,
        };

        setChatMessages((prevMessages) => [...prevMessages, message]); // 메시지를 상태에 추가
        stompClient.send("/pub/chat/message", { token: accessToken }, JSON.stringify(message)); // 메시지 전송
    } catch (error) {
        console.error("이미지 업로드에 실패했습니다:", error);
        alert("이미지 업로드에 실패했습니다.");
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
      <button 
        onClick={handleSendLocation}
        className="ml-2 px-4 py-2 bg-green-500 text-white rounded"
      >
      위치 전송
      </button>
      <button 
        onClick={handleFileSelect}
        className="ml-2 px-4 py-2 bg-sky-500 text-white rounded"
      > 
        사진 전송
      </button>
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        style={{ display: 'none' }} // 숨김 처리
      />
      <ul className="space-y-4">
        {chatMessages.slice().reverse().map((message) => (
          <li key={`${message.messageId}-${chatRoom.id}`} className="border p-2 bg-white rounded shadow-sm">
            <div>
              <strong>보낸 이:</strong> {message.sender}
            </div>
            <div>
              <strong>메시지 내용:</strong> {message.message}
            </div>
            {(message.latitude !== 0 && message.longitude !== 0) ? (
              <div>
                <strong>위치 정보:</strong> 
                <div>위도: {message.latitude}, 경도: {message.longitude}</div>
                <a
                  href={`https://www.google.com/maps?q=${message.latitude},${message.longitude}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-500"
                >
                  구글 맵에서 보기
                </a>
              </div>
            ) : null}
            {message.image && (
              <div>
                <strong>이미지:</strong> <img src={message.image} alt="메시지 첨부 이미지" className="max-w-full h-auto" />
              </div>
            )}
            <div>ㅇ
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