"use client";

import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { use } from "react";

export default function ClientPage() {
  const router = useRouter();
  const { loginMember } = use(LoginMemberContext);

  return (
    <div className="flex flex-col items-center p-6 w-full">
      <h2 className="text-2xl font-bold mb-4">내 정보 조회</h2>
      <div className="border p-4 rounded-md w-1/2 shadow-lg">
        <div className="mb-2">
          <strong>아이디:</strong> {loginMember.username}
        </div>
        <div className="mb-2">
          <strong>이메일:</strong> {loginMember.email}
        </div>
        <div className="mb-2">
          <strong>닉네임:</strong> {loginMember.nickname}
        </div>
        <div className="mb-2">
          <strong>주소:</strong> {loginMember.address || "주소 없음"}
        </div>
        <div className="mb-4">
          <strong>프로필 사진:</strong> <br />
          {loginMember.profileUrl ? (
            <img
              src={loginMember.profileUrl}
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
