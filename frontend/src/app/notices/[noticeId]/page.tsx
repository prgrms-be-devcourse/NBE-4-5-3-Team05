"use client";

import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import Link from "next/link";
import { useParams, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function Page() {
  const { noticeId }: { noticeId: string } = useParams();

  const [notice, setNotice] =
    useState<components["schemas"]["NoticeResBody"]>();

  useEffect(() => {
    const getNotice = async () => {
      const result = await client.GET("/api/admin/notices/{notice-id}", {
        params: {
          path: {
            "notice-id": noticeId!,
          },
        },
      });
      if (result.error) {
        console.log(result.error);
        return;
      }
      setNotice(result.data.data);
    };
    getNotice();
  }, []);
  return (
    <div className="w-full flex-1 flex flex-col justify-center items-center">
      <div className="flex flex-col flex-1 border rounded-2 border-gray-300 w-3/4">
        <div className="w-full p-10 flex flex-col flex-1">
          <div className="w-full py-5  border-b-2 flex justify-between items-center">
            <p className="font-bold text-3xl">공지사항</p>
            <Link className="font-2xl" href={"/posts"}>
              뒤로 가기
            </Link>
          </div>
          <div className="w-full pt-5 text-2xl">{notice?.title}</div>
          <div className="w-full mt-10 flex-1 border-4 rounded-2xl p-10">
            {notice?.content}
          </div>
        </div>
      </div>
    </div>
  );
}
