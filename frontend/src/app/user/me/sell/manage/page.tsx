import client from "@/lib/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{
    page: number;
    pageSize: number;
    sort: string;
    status: "RESERVED" | "AVAILABLE" | "PURCHASED" | undefined;
  }>;
}) {
  const { page = 1, pageSize = 10, sort = "desc", status } = await searchParams;

  const response = await client.GET("/api/posts/my", {
    params: {
      query: {
        page,
        pageSize,
        sort,
        ...(status ? { status } : {}),
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

  const postInfo = rsData.data.items;
  const pageInfo = {
    totalPages: rsData.data.totalPages,
    totalItems: rsData.data.totalItems,
    currentPage: rsData.data.curPageNo,
    pageSize,
  };

  return <ClientPage postInfo={postInfo} pageInfo={pageInfo} />;
}
