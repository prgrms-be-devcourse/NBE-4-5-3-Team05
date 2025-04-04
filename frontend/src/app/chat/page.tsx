import { cookies } from "next/headers";
import ClientPage from "./ClientPage";
import client from "@/lib/client";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{
    receiver: string;
  }>;
}) {
  const { receiver = "" } = await searchParams;

  const cookie = (await cookies()).toString();

  const response = await client.GET("/api/chat/rooms", {
    headers: {
      cookie: cookie,
    },
    credentials: "include",
  });

  if (response.error) {
    return <div>{response.error.message}</div>;
  }
  console.log("response", response);

  const rsData = response.data!!;
  const chatRoom = rsData.data;
  let searchChatRoomDto = chatRoom;
  if (receiver) {
    const searchResponse = await client.GET("/api/chat/search", {
      headers: {
        cookie: cookie,
      },
      params: {
        query: {
          receiver: receiver,
        },
      },
      credentials: "include",
    });

    if (searchResponse.error) {
      console.error("검색 오류, ", searchResponse.error.message);
    } else {
      const data = searchResponse.data.data; // 단일 객체 인지 또는 배열인지 확인
      searchChatRoomDto = Array.isArray(data) ? data : data ? [data] : [];
    }
  }

  return (
    <ClientPage
      chatRoom={chatRoom}
      searchChatRoomDto={searchChatRoomDto}
      receiver={receiver}
      cookie={cookie}
    />
  );
}
