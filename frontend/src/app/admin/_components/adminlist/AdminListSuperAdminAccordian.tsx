"use client";

import { Button } from "@/components/ui/button";
import client from "@/lib/client";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@radix-ui/react-accordion";
import { ChevronDown } from "lucide-react";
import { AdminListSuperAdminItem } from "@/app/_type/AdminListSuperAdminItem";
import { useState } from "react";

export default function AdminListSuperAdminAccordian({
  items,
}: {
  items: AdminListSuperAdminItem[];
}) {
  const deleteAdmin = async (item: AdminListSuperAdminItem) => {
    const userConfirmed = window.confirm(
      "정말로 이 관리자를 삭제하시겠습니까?"
    );
    if (!userConfirmed) return;

    // path parameter를 활용하여 DELETE 요청 전달
    const response = await client.DELETE("/api/admin/{adminId}", {
      params: {
        path: {
          adminId: item.id,
        },
      },
      credentials: "include",
    });

    if (response.error) {
      console.error(response.response);
      alert("관리자 삭제에 실패했습니다.");
      return;
    }
    window.location.reload();
  };

  return (
    <div className="w-full mx-auto overflow-y-auto border rounded-lg px-2 flex-1 flex flex-col">
      <Accordion
        type="multiple"
        className="w-full mx-auto mt-5 flex-1 flex flex-col"
      >
        {items.map((item) => (
          <AccordionItem
            key={item.id}
            value={String(item.id)}
            className="border rounded-lg border-gray-300 my-2"
          >
            <AccordionTrigger className="w-full flex justify-between px-3 text-xl hover:cursor-pointer py-4">
              <p>{item.id}</p>
              <ChevronDown className="ml-2" size={20} />
            </AccordionTrigger>
            <AccordionContent className="flex justify-between p-6 min-h-48 bg-gray-300">
              <div className="flex flex-col space-y-2">
                <p>아이디: {item.username}</p>
                <p>닉네임: {item.nickname}</p>
                <p>이메일: {item.email}</p>
                <p>권한: {item.role}</p>
                <p>프로필: {item.profileUrl}</p>
                <p>생성일: {item.createdAt}</p>
                <p>수정일: {item.modifiedAt}</p>
              </div>
              <div className="flex flex-col items-end justify-center">
                <Button
                  variant="destructive"
                  className="hover:cursor-pointer"
                  onClick={(e) => {
                    e.preventDefault();
                    deleteAdmin(item);
                  }}
                >
                  관리자 삭제
                </Button>
              </div>
            </AccordionContent>
          </AccordionItem>
        ))}
      </Accordion>
    </div>
  );
}
