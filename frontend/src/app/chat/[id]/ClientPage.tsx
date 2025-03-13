"use client";

import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/backend/client";

export default function ClientPage({
  messages,
  title,
  roomId,
  cookie,
}:{
  messages:components["schemas"]["MessageDto"][];
  title:string;
  roomId: string;
  cookie: string;
}) {
    const deleteRoom = async () => {
        const response = await client.DELETE(`/api/chat/message`, { // 이때 URL 경로는 고정
            headers: {
                cookie: cookie,
            },
            params: { 
                query:{
                    roomId,
                }
            },
            credentials: 'include',
        });
        const rsData=response.data!!;        

        if (rsData.code!="200") {
            console.error("삭제 실패:", rsData.message);
            alert("채팅방 삭제에 실패했습니다.");
        } else {
            alert("채팅방 삭제가 완료되었습니다.");
            window.location.href = "/chat"; // 메인 페이지로 리다이렉트
        }
    };

    const handleDelete = () => {
        if (window.confirm("메세지가 삭제됩니다. 정말로 나가시겠습니까? ")) {
          deleteRoom(); // 사용자 확인 시 삭제 호출
        }
    };
    
  return (
    <div>
        <h1 className="text-xl font-bold mb-4">{title}</h1>
        <button 
          onClick={handleDelete} // 삭제 함수 호출
          className="mb-4 px-4 py-2 bg-red-600 text-white rounded"
        >
          채팅방 나가기
        </button>
        <ul className="space-y-4">
            {messages.slice().reverse().map((message) => (
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
                        <strong>보낸 시간:</strong> {message.timestamp} {/* 포맷 할 경우 */}
                    </div>
                </li>
            ))}
        </ul>
    </div>
);
}