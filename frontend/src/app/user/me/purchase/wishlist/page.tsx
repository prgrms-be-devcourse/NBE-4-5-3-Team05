import RequireAuthenticated from "@/components/auth/RequireAuthenticated";
import client from "@/lib/client";
import { cookies } from "next/headers";
import ClientPage from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{
    page: number;
    pageSize: number;
  }>;
}) {
  const { page = 1, pageSize = 5 } = await searchParams;

  const response = await client.GET("/api/posts/my/favorites", {
    params: {
      query: {
        page,
        pageSize,
      },
    },
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  if (response.error) {
    console.log("서버 오류 : " + response.error.message);
    return;
  }

  const rsData = response.data;
  console.log(rsData);

  const postInfo = rsData.data.items;
  const pageInfo = {
    totalPages: rsData.data.totalPages,
    totalItems: rsData.data.totalItems,
    currentPage: rsData.data.curPageNo,
    pageSize,
  };

  return <ClientPage postInfo={postInfo} pageInfo={pageInfo} />;
}
