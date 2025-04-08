"use client";

import { components } from "@/lib/backend/apiV1/schema";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react"; 
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"; 
import { faMagnifyingGlass,faHeadphones } from "@fortawesome/free-solid-svg-icons";
import { Button } from "@/components/ui/button";
import client from "@/lib/client";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";

export default function ClientPage({
  chatRoom,
  searchChatRoomDto,
  receiver,
  cookie,
}:{
  chatRoom:components["schemas"]["ChatRoomDto"][];
  searchChatRoomDto:components["schemas"]["ChatRoomDto"][];
  receiver:string;
  cookie:string;
}) {
  const router = useRouter();
  const [searchTerm, setSearchTerm] = useState(receiver); 
  const [isSearchVisible, setIsSearchVisible] = useState(false);
  const [adminRoom, setAdminRoom] = useState(null); 
  const [chatRooms, setChatRooms] = useState(chatRoom); // 채팅방 목록 상태
  const [stompClient, setStompClient] = useState<any>(null); // WebSocket 클라이언트 상태

  // WebSocket 연결 및 채팅방 목록 업데이트 구독
  useEffect(() => {
    const fetchUserInfoAndConnect = async () => {
      try {
        const userResponse = await client.GET("/api/chat/user", {
          headers: { cookie },
          credentials: "include",
        });

        if (userResponse.data?.code === "200") {
          const accessToken = userResponse.data.data.token || "";
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

              // 채팅방 목록 업데이트 구독
              stompClient.subscribe(`/sub/chat/rooms`, (message) => {
                const updatedRooms = JSON.parse(message.body);
                console.log('업데이트된 채팅방:', updatedRooms);

                // 기존 채팅방 목록과 병합하여 업데이트
                setChatRooms(prevRooms => {
                  const updatedRoomMap = new Map(updatedRooms.map((room: components["schemas"]["ChatRoomDto"]) => [room.roomId, room]));
                  return prevRooms.map(room => updatedRoomMap.get(room.roomId) || room);
                });
              });
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
  }, [cookie]);

  // 검색 결과 동기화
  const filteredRooms = receiver ? chatRooms.filter((room: components["schemas"]["ChatRoomDto"]) => room.other?.includes(receiver)) : chatRooms;

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault(); 
    router.push(`/chat?page=1&receiver=${searchTerm}`);
  };

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter') {
      handleSearch(event as React.FormEvent); 
    }
  };

  const handleCreateAdminRoom= async()=>{
    let adminRoom;
    const createAdminRoomResponse = await client.POST(`/api/chat/admin`,{
      headers: {
          cookie: cookie,
      },
      credentials: "include",
    });
    
    const adminRsdata= createAdminRoomResponse.data!!;
    if (adminRsdata.code!="200") {
        console.error("채팅방 생성 오류: ", adminRsdata.message);
    } else {
        // 여기서 data를 체크하여 null 여부를 확인
        adminRoom=adminRsdata.data;
        window.location.reload();
        console.log("채팅방 생성 성공:", adminRoom);
    }
  };

  // 타입에 따른 디스플레이
  const getDisplayMessage = (messageType: string | undefined, lastMessage: string | undefined) =>{
    if(messageType === "LOCATION"){
      return "위치를 전송했습니다.";
    }else if(messageType === "IMAGE"){
      return "사진을 전송했습니다.";
    }else{
      return lastMessage || "";
    }
  }

  // 렌더링할 채팅방 목록
  const displayedRooms = filteredRooms;
  
  return (
    <div className="w-full">
      <h1 className="text-xl font-bold mb-4">채팅방 목록</h1>
        <div className="mb-4 flex justify-end">
          <Button onClick={handleCreateAdminRoom} variant="outline" className="ml-auto">
            <FontAwesomeIcon icon={faHeadphones} />
            고객센터
          </Button>
    </div>
    
    <form onSubmit={handleSearch} className="mb-4 flex items-center">
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)} // 검색어 상태 업데이트
          placeholder="검색어를 입력하세요"
          onKeyPress={handleKeyPress} // 엔터 키 인식
          className="border-2 border-gray-300 rounded-lg p-2 flex-grow"
        />
        <Button
          type="submit"
          variant="outline"
          className="ml-1 px-5 py-5 rounded-lg">
          <FontAwesomeIcon icon={faMagnifyingGlass} />
          검색
        </Button>
    </form>

    {receiver && (
        <div>
          <h2 className="text-lg font-semibold mb-2">검색 결과: {receiver}</h2>
          {displayedRooms.length > 0 ? (
            <div className="border-2 border-gray-300 rounded-lg p-4 bg-white shadow-md">
              {displayedRooms.map((room) => (
                <Link key={room.roomId} href={`/chat/${room.roomId}`} className="block">
                  <div><strong>{room.name}</strong></div>
                  <div className="text-gray-600 text-sm">
                    {getDisplayMessage(room.messageType ,room.lastMessage)}
                  </div>
                  <div className="text-gray-500 text-xs text-right">
                    {room.lastTimestamp}
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div>검색 결과가 없습니다.</div>
          )}
        </div>
      )}

     {!receiver && (
      <ul className="space-y-4">
        {displayedRooms.map((room) => (
          <li key={room.roomId} className="border-2 border-gray-300 rounded-lg p-4 bg-white shadow-md">
            <Link href={`/chat/${room.roomId}`} className="block">
              <div>
                <strong className="text-lg">{room.other}</strong>
              </div>
              <div className="text-gray-600 text-sm">
                {getDisplayMessage(room.messageType ,room.lastMessage)}
              </div>
              <div className="text-gray-500 text-xs text-right">
                {room.lastTimestamp}
              </div>
            </Link>
          </li>
        ))}
      </ul>
    )}
    </div>
  );
}