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
    const roomId = params.id; 
    
    const token = (await cookies()).get("accessToken")?.value;
    console.log("token: ",token);
    const cookie= (await cookies()).toString();

    const messageResponse = await client.GET("/api/chat/message", {
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

    const roomResponse =await client.GET("/api/chat/room/{roomId}",{
      headers: {
          cookie:cookie,
        },
        params: {
          path:{
            roomId,
          },
        },
        credentials:"include",
    });
    
    const roomData=roomResponse.data!!;
    if(!roomData || roomData.code!="200"){
      return {
        error: roomData?.message || "Room data is not available.",
      };
    }
    const chatRoom=roomData.data;

    const messageData = messageResponse.data!!; // 변환할 데이터
    const title = messageData.message;
    const messages = messageData.data; // 메시지 데이터
  
    return <ClientPage messages={messages} title={title} roomId={roomId} cookie={cookie} chatRoom={chatRoom}/>; // 메시지 데이터 클라이언트 페이지로 전달

}