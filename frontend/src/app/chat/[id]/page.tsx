import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";

export default async function Page({
    params,
}: {
    params: {
        id: string;
    }
}) {
    const roomId = await params.id; 
    const token = (await cookies()).get("accessToken")?.value;
    console.log("token: ",token);
    const cookie= (await cookies()).toString();
    // 채팅방 메시지 API 호출
    const response = await client.GET("/api/chat/message", {
        headers: {
          cookie:cookie,
        },
        params: {
          query:{
            roomId,
          },
        },
        credentials:"include",
    });

    if (response.error) {
        return <div>{response.error.message}</div>; // API 응답이 실패한 경우 처리
    }
    
    console.log("response:",response);

    const rsData = response.data!!; // 변환할 데이터
    const title = rsData.message;
    console.log("어떤방?: ",title);
    const messages = rsData.data; // 메시지 데이터

    

  
    return <ClientPage messages={messages} title={title} roomId={roomId} cookie={cookie}/>; // 메시지 데이터 클라이언트 페이지로 전달
}