import { cookies } from "next/headers";
import ClientPage from "./ClientPage";
import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";

export default async function Page(
  { searchParams }:
  {
     searchParams: { 
      receiver: string 
    };
  }
){

  const {
    receiver=""
  } = await searchParams;

  const cookie = (await cookies()).toString();

  const response= await client.GET("/api/chat/rooms",{
    headers:{
      cookie:cookie,
    },
    credentials:"include",
  });

  if(response.error){
    return <div>{response.error.message}</div>
  }
  console.log("response",response);
  
  const rsData=response.data!!;
  const chatRoom=rsData.data;  
  let searchChatRoomDto=chatRoom;
  if(receiver){
    const searchResponse = await client.GET("/api/chat/search",{
      headers:{
        cookie:cookie,
      },
      params:{
        query:{
          receiver: receiver,
        },
      },
      credentials:"include",
    });
  
    if(searchResponse.error){
      console.error("검색 오류, ",searchResponse.error.message);
    }else{
      const data = searchResponse.data.data; // 단일 객체 인지 또는 배열인지 확인
      searchChatRoomDto = Array.isArray(data) ? data : data ? [data] : [];
    }
  }

  const handleCreateAdminRoom= async()=>{
    const adminId = "user-4e8d79c9-1adc-4bbf-b30f-8e251635bdcb"; 
    let adminRoom;
    const createAdminRoomResponse = await client.POST(`/api/chat/admin/{adminId}`,{
      headers: {
          cookie: cookie,
      },
      params:{
        path: {
          adminId:adminId,
        },
      },
      credentials: "include",
    });
    
    const adminRsdata= createAdminRoomResponse.data!!;
    if (adminRsdata.code!="200") {
        console.error("채팅방 생성 오류: ", adminRsdata.message);
    } else {
        // 여기서 data를 체크하여 null 여부를 확인
        adminRoom=adminRsdata.data;    
        console.log("채팅방 생성 성공:", adminRoom);
    }
  };

  return (<ClientPage chatRoom={chatRoom} searchChatRoomDto={searchChatRoomDto} receiver={receiver} cookie={cookie}/>);
}
