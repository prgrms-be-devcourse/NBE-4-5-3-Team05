import client from "@/lib/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";

export default async function Page({
  params,
}: {
  params: Promise<{
    id: string;
  }>;
}) {
  const { id } = await params;
  const roomId = id;
  const token = (await cookies()).get("accessToken")?.value;
  console.log("token: ", token);
  const cookie = (await cookies()).toString();

  const messageResponse = await client.GET("/api/chat/message", {
    headers: {
      cookie: cookie,
    },
    params: {
      query: {
        roomId,
      },
    },
    credentials: "include",
  });
  console.log("res1:", messageResponse);

  const roomResponse = await client.GET("/api/chat/room/{roomId}", {
    headers: {
      cookie: cookie,
    },
    params: {
      path: {
        roomId,
      },
    },
    credentials: "include",
  });
  console.log("res2:", roomResponse);

  const roomData = roomResponse.data!!;

  if (!roomData || roomData.code != "200") {
    return {
      error: roomData?.message || "Room data is not available.",
    };
  }
  const chatRoom = roomData.data;

  const messageData = messageResponse.data!!; // 변환할 데이터
  const title = messageData.message;
  const messages = messageData.data; // 메시지 데이터
  console.log("message:", messages);
  return (
    <ClientPage
      messages={messages}
      title={title}
      roomId={roomId}
      cookie={cookie}
      chatRoom={chatRoom}
    />
  ); // 메시지 데이터 클라이언트 페이지로 전달
}
