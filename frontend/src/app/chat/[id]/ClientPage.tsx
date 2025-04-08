"use client";

import { components } from "@/lib/backend/apiV1/schema";
import { use, useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import { headers } from "next/headers";
import { Textarea } from "@/components/ui/textarea";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowRight,
  faPaperclip,
  faImage,
  faLocationDot,
  faRightFromBracket,
  faBars,
  faTrash,
} from "@fortawesome/free-solid-svg-icons";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import client from "@/lib/client";
import MapComponent from "@/components/MapComponent";

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
  const [chatMessages, setChatMessages] =
    useState<components["schemas"]["MessageDto"][]>(messages); // 초기 메시지로 설정
  const [userNickname, setUserNickname] = useState<string>("");
  const [accessToken, setAccessToken] = useState<string>("");
  const [stompClient, setStompClient] = useState<any>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const messagesEndRef = useRef<null | HTMLDivElement>(null);

  const [isOpen, setIsOpen] = useState(false);

  const [latitude, setLatitude] = useState(0);
  const [longitude, setLongitude] = useState(0);
  const [zoom, setZoom] = useState(18);

  // 2️⃣ 팝업 열기 버튼 핸들러
  const openPopup = () => {
    setIsOpen(true);
  };

  // 3️⃣ 팝업 닫기 버튼 핸들러
  const closePopup = () => {
    setIsOpen(false);
  };

  useEffect(() => {
    const fetchUserInfoAndConnect = async () => {
      try {
        const userResponse = await client.GET("/api/chat/user", {
          headers: { cookie },
          credentials: "include",
        });

        if (userResponse.data?.code === "200") {
          setUserNickname(userResponse.data.data.name || "Unknown");
          setAccessToken(userResponse.data.data.token || "");
          const protocol = `${process.env.NEXT_PUBLIC_PROTOCOL}`;
          let url = `${protocol}://${process.env.NEXT_PUBLIC_BACKEND_HOST}`;
          if (protocol === "http") {
            url += `:${process.env.NEXT_PUBLIC_BACKEND_PORT}`;
          }
          url += "/ws-stomp";
          const socket = new SockJS(url);
          const stompClient = Stomp.over(socket);
          setStompClient(stompClient);

          console.log("연결 시도 중...");
          stompClient.connect(
            { token: accessToken },
            (frame: string) => {
              console.log("연결 완료!", frame);

              stompClient.subscribe(
                `/sub/chat/room/${roomId}`,
                async (message: { body: string }) => {
                  const messageData = JSON.parse(message.body);
                  console.log("수신된 메시지: ", messageData);

                  // 서버에서 최신 메시지를 가져오기 위한 API 호출
                  const messageResponse = await client.GET(
                    "/api/chat/message",
                    {
                      headers: { cookie },
                      params: {
                        query: { roomId },
                      },
                      credentials: "include",
                    }
                  );

                  if (messageResponse.data?.code === "200") {
                    setChatMessages(messageResponse.data.data); // 수신된 메시지로 상태 업데이트
                    scrollToBottomWithOffset(50);
                  }
                }
              );
            },
            (error: any) => {
              alert("WebSocket 연결 실패했습니다. 다시 시도해 주세요.");
            }
          );
        } else {
          alert("다시 로그인해 주세요.");
          window.location.href = "/member/login"; // 로그인 페이지로 리다이렉트
        }
      } catch (error) {
        alert("다시 로그인해 주세요.");
        window.location.href = "/member/login"; // 로그인 페이지로 리다이렉트
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

  useEffect(() => {
    getCurrentPosition();
  }, []);

  useEffect(() => {
    scrollToBottomWithOffset(50);
  }, [chatMessages]);

  const scrollToBottomWithOffset = (offset: number) => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
      window.scrollBy(0, offset); // 지정된 offset만큼 스크롤을 더 내립니다.
    }
  };

  const handleInputChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
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
      latitude: 0,  
      longitude: 0, 
    };

    stompClient.send(
      "/pub/chat/message",
      { token: accessToken },
      JSON.stringify(message)
    );
    setInputMessage("");

    setChatMessages((prevMessages) => [...prevMessages, message]);
    scrollToBottomWithOffset(50); // 스크롤 이동
  };
  const handleKeyPress = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === "Enter") {
      handleSendMessage();
    }
  };

  const deleteRoom = async () => {
    try {
      await client.DELETE(`/api/chat/message`, {
        headers: { cookie },
        params: { query: { roomId } },
        credentials: "include",
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

    getCurrentPosition();

    const message = {
      roomId,
      sender: userNickname,
      type: "LOCATION",
      latitude: latitude, // 위도
      longitude: longitude, // 경도
      messageId: null, // 필요 시 비워둡니다
      image: null, // 필요 시 비워둡니다
      timestamp: new Date().toISOString(), // 현재 시간 설정
    };

    stompClient.send(
      "/pub/chat/message",
      { token: accessToken },
      JSON.stringify(message)
    );
    alert("위치가 전송되었습니다!");
    return;
  };

  const getCurrentPosition = () => {
    navigator.geolocation.getCurrentPosition((position) => {
      setLatitude(position.coords.latitude);
      setLongitude(position.coords.longitude);
    });
  };

  const handleFileSelect = () => {
    fileInputRef.current?.click(); // 파일 입력 클릭
  };

  const handleFileChange = async (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file); // 파일 추가

    try {
      // fetch를 사용하여 파일 업로드
      const protocol = `${process.env.NEXT_PUBLIC_PROTOCOL}`;
      let url = `${protocol}://${process.env.NEXT_PUBLIC_BACKEND_HOST}`;
      if (protocol === "http") {
        url += `:${process.env.NEXT_PUBLIC_BACKEND_PORT}`;
      }
      url += `/api/uploadFile`;
      const response = await fetch(url, {
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
        latitude: 0,  
        longitude: 0,
      };

      setChatMessages((prevMessages) => [...prevMessages, message]); // 메시지를 상태에 추가
      stompClient.send(
        "/pub/chat/message",
        { token: accessToken },
        JSON.stringify(message)
      ); // 메시지 전송
    } catch (error) {
      console.error("이미지 업로드에 실패했습니다:", error);
      alert("이미지 업로드에 실패했습니다.");
    }
  };

  return (
    <div className="flex flex-col h-screen w-full">
      <h1 className="text-xl font-bold mb-4 text-center">{title}</h1>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="outline"
            className="flex justify-end gap-3 px-4 py-4 rounded-md mb-4"
            style={{ marginLeft: "auto" }}
          >
            <FontAwesomeIcon icon={faBars} />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-48 bg-white border border-gray-300 shadow-lg rounded-md">
          <DropdownMenuItem
            onClick={() => (window.location.href = "/chat")}
            className="flex items-center"
          >
            <FontAwesomeIcon icon={faRightFromBracket} className="mr-2" />
            채팅방 나가기
          </DropdownMenuItem>
          <DropdownMenuItem
            onClick={handleDelete}
            className="flex items-center"
          >
            <FontAwesomeIcon icon={faTrash} className="mr-2" />
            채팅방 비우기
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <div className="flex-grow overflow-auto flex flex-col mb-20 w-full">
        <ul className="space-y-3 w-full">
          {chatMessages.map((message) => (
            <li
              key={`${message.messageId}-${chatRoom.id}`}
              className={`border p-2 rounded shadow-sm text-sm
        ${
          message.sender === userNickname
            ? "bg-gray-200 text-right w-3/4 ml-auto"
            : "bg-white text-left w-3/4 mr-auto"
        }`}
            >
              {message.sender !== userNickname && (
                <div className="flex justify-start">
                  {" "}
                  {/* 수신한 사람의 이름을 왼쪽 정렬 */}
                  <span className="font-semibold">{message.sender}</span>
                </div>
              )}

              <div
                className={`my-1 ${
                  message.sender === userNickname ? "text-right" : "text-left"
                }`}
              >
                {" "}
                {/* 메시지 내용의 정렬 */}
                {message.message}
              </div>

              {/* 위도, 경도 정보 표시 */}
              {message.latitude !== 0 && message.longitude !== 0 ? (
                <div className="w-auto h-[30vh] flex flex-col items-end">
                  <div className=" h-[30vh] w-[30vh]">
                    <MapComponent
                      currentPos={{
                        lat: message.latitude!,
                        lng: message.longitude!,
                        zoom: 18,
                      }}
                      onLocationSelect={() => {}}
                    ></MapComponent>
                  </div>
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
                  <img
                    src={message.image}
                    alt="메시지 첨부 이미지"
                    className="max-w-full h-auto"
                  />
                </div>
              )}

              <div
                className={`text-gray-500 text-xs ${
                  message.sender === userNickname ? "text-left" : "text-right"
                }`}
              >
                {message.timestamp}
              </div>
            </li>
          ))}
        </ul>
        <div ref={messagesEndRef} />
      </div>
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        style={{ display: "none" }}
      />

      <footer className="fixed bottom-0 left-0 right-0 bg-white p-4 border-t flex items-center">
        <div>
          {/* 팝업이 열려 있다면 렌더링 */}
          {isOpen && (
            <div
              style={{
                position: "fixed",
                top: 0,
                left: 0,
                width: "50vh",
                height: "50vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                backgroundColor: "rgba(0, 0, 0, 0.3)",
              }}
            >
              <div
                className="w-full h-full z-10"
                style={{
                  backgroundColor: "#fff",
                  border: "1px solid #ccc",
                  borderRadius: "4px",
                }}
              >
                <MapComponent
                  currentPos={{ lat: latitude, lng: longitude, zoom: zoom }}
                  onLocationSelect={(lat, lng, zoom) => {
                    setLatitude(lat);
                    setLongitude(lng);
                    setZoom(zoom);
                  }}
                ></MapComponent>
                <div className="flex w-full justify-between">
                  <Button
                    onClick={(e) => {
                      handleSendLocation();
                      closePopup();
                    }}
                  >
                    전송
                  </Button>
                  <Button onClick={closePopup}>취소</Button>
                </div>
              </div>
            </div>
          )}
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="outline"
              className="rounded-md flex items-center px-3 h-15"
            >
              <FontAwesomeIcon icon={faPaperclip} />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-48 bg-white border border-gray-300 shadow-lg rounded-md">
            <DropdownMenuLabel>
              <strong>첨부 파일</strong>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            {/* 팝업 열기 버튼 */}
            <button onClick={openPopup}>팝업 열기</button>

            <DropdownMenuItem
              onClick={() => setIsOpen(true)}
              className="flex items-center"
            >
              <FontAwesomeIcon icon={faLocationDot} className="mr-2" />
              위치
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={handleFileSelect}
              className="flex items-center"
            >
              <FontAwesomeIcon icon={faImage} className="mr-2" />
              사진
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
        <Textarea
          value={inputMessage}
          onChange={handleInputChange}
          onKeyPress={handleKeyPress}
          placeholder="메시지를 입력하세요."
          className="border p-2 flex-grow mx-2 rounded-md resize-none"
        />
        <Button
          variant="outline"
          onClick={handleSendMessage}
          className="rounded-md flex items-center h-15 px-3"
        >
          <FontAwesomeIcon icon={faArrowRight} />
        </Button>
      </footer>
    </div>
  );
}

