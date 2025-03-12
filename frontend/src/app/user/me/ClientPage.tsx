"use client";

import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";
import { useRouter } from "next/navigation";
import router from "next/router";

export default function ClientPage({
  userInfo,
}: {
  userInfo: components["schemas"]["UserDto"];
}) {
  const router = useRouter();

  return (
    <div className="flex flex-col items-center p-6">
      <h2 className="text-2xl font-bold mb-4">내 정보 조회</h2>
      <div className="border p-4 rounded-md w-[400px] shadow-lg">
        <div className="mb-2">
          <strong>아이디:</strong> {userInfo.username}
        </div>
        <div className="mb-2">
          <strong>이메일:</strong> {userInfo.email}
        </div>
        <div className="mb-2">
          <strong>닉네임:</strong> {userInfo.nickname}
        </div>
        <div className="mb-2">
          <strong>주소:</strong> {userInfo.address || "주소 없음"}
        </div>
        <div className="mb-4">
          <strong>프로필 사진:</strong> <br />
          {userInfo.profileUrl ? (
            <img
              src={userInfo.profileUrl}
              alt="프로필"
              className="w-24 h-24 rounded-full mt-2"
            />
          ) : (
            <span>없음</span>
          )}
        </div>
        <Button
          onClick={() => router.push("/user/me/modify")}
          className="w-full bg-blue-500 text-white"
        >
          내 정보 수정
        </Button>
      </div>
    </div>
  );
}
