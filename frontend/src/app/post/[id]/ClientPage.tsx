"use client"

import { components } from "@/lib/backend/apiV1/schema";
import { useRouter } from "next/navigation";

export default function ClientPage({
    post,
    chatRoom,
}: {
    post:components["schemas"]["ProductPostResponse"];
    chatRoom:components["schemas"]["ChatRoom"];
})
    {
        const router = useRouter();

        const handleChatRoomNavigation = () => {
            if (chatRoom && chatRoom.id) {
                router.push(`/chat/${chatRoom.roomId}`); // 생성된 채팅방으로 이동
            } else {
                alert("채팅방 정보가 없습니다.");
            }
        };
    return <div>
            <button 
                onClick={handleChatRoomNavigation} 
                className="mt-4 px-4 py-2 bg-blue-500 border-2 border-blue-700 text-white rounded hover:bg-blue-700 transition duration-300"
            >채팅하기</button>
            <div>id:{post.id} </div>
            <div>작성자 id:{post.writerId} </div>
            <div>작성자:{post.writerName} </div>
            <div>상품 이름:{post.productName} </div>
            <div>상품 가격:{post.productPrice} </div>
            <div>제목:{post.title} </div>
            <div>내용:{post.content} </div> 
            <div>이미지:{post.imageUrls} </div>
            <div><strong>위치</strong>
                <div>   위도:{post.latitude}</div>
                <div>   경도:{post.longitude}</div>
            </div>
            <div>카테고리:{post.categories} </div>
     </div>;
}
