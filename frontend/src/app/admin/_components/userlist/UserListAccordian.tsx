import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import { motion } from "framer-motion";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@radix-ui/react-accordion";
import { ChevronDown } from "lucide-react";
import { UserListItem } from "@/app/_type/UserListItem";
import { Input } from "@/components/ui/input";
import { useState } from "react";

export default function UserListAccordian({
  items,
}: {
  items: UserListItem[];
}) {
  const [reason, setReason] = useState<string>("");
  const doBan = async (item: UserListItem) => {
    const response = await client.POST("/api/admin/users/{user-id}/ban", {
      params: {
        path: {
          "user-id": item.id!,
        },
      },
      body: {
        reason: reason,
      },
      credentials: "include",
    });
    if (response.error) {
      console.log(response.response);
    }
    window.location.reload();
  };

  const doUnBan = async (item: UserListItem) => {
    const response = await client.POST("/api/admin/users/{user-id}/ban", {
      params: {
        path: {
          "user-id": item.id!,
        },
      },
      body: {
        reason: reason,
      },
      credentials: "include",
    });
    if (response.error) {
      console.log(response.response);
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
            className="border rounded-lg border-gray-300 my-2 "
          >
            <AccordionTrigger className="w-full flex justify-between px-3 text-xl hover:cursor-pointer py-4">
              <p>{item.id}</p>
              <ChevronDown className="ml-2" size={20} />
            </AccordionTrigger>

            <AccordionContent className="flex justify-between p-6 min-h-48 bg-gray-300 ">
              <div className="flex-col justify-between">
                <p>아이디 : {item.username}</p>
                <p>닉네임 : {item.nickname}</p>
                <p> 이메일 : {item.email}</p>
                <p> 권한 : {item.role}</p>
                <p>프로필 : {item.profileUrl}</p>
                <p>계정 정지 여부 : {item.blocked ? "true" : "false"}</p>
                <p> 계정 정지 횟수 : {item.blockedCount}</p>
                <p>회원가입 일자 : {item.createdAt}</p>
                <p>회원정보 수정 일자 : {item.modifiedAt}</p>
              </div>
              <div>
                <Input
                  className="my-2"
                  placeholder="정지 사유를 입력하세요"
                  disabled={item.blocked}
                  onChange={(e) => {
                    e.preventDefault();
                    setReason(e.target.value);
                  }}
                ></Input>
                <Button
                  variant={"destructive"}
                  className="mx-2 hover:cursor-pointer"
                  disabled={item.blocked}
                  onClick={(e) => {
                    e.preventDefault();
                    const userConfirmed =
                      window.confirm("정말로 정지하시겠습니까?");
                    if (userConfirmed) {
                      doBan(item);
                      // 여기에 실행할 동작을 추가하세요
                    } else {
                      console.log("사용자가 취소를 클릭했습니다.");
                    }
                  }}
                >
                  유저 정지
                </Button>
                <Button
                  variant={"secondary"}
                  className="hover:cursor-pointer"
                  disabled={item.blocked == false}
                  onClick={(e) => {
                    e.preventDefault();
                    const userConfirmed =
                      window.confirm("정지를 해제하시겠습니까?");
                    if (userConfirmed) {
                      doUnBan(item);
                      // 여기에 실행할 동작을 추가하세요
                    } else {
                      console.log("사용자가 취소를 클릭했습니다.");
                    }
                  }}
                >
                  정지 해제
                </Button>
              </div>
            </AccordionContent>
          </AccordionItem>
        ))}
      </Accordion>
    </div>
  );
}
