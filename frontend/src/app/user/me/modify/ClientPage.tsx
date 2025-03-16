"use client";

import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
import { useRouter } from "next/navigation";
import { use } from "react";

export default function ClientPage() {
  const router = useRouter();
  const { loginMember, setLoginMember } = use(LoginMemberContext);

  async function updateInfo(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();

    const formData = e.target as HTMLFormElement;
    console.log(formData);
    const email = formData.email.value;
    const nickname = formData.nickname.value;
    const address = formData.address.value;
    const profileUrl = formData.profileUrl.value;

    const response = await client.PUT("/api/users/me", {
      body: {
        email,
        nickname,
        address,
        profileUrl,
      },
      credentials: "include",
    });

    if (response.error) {
      alert(response.error.message);
      router.push("/user/login");
      return;
    }

    alert("사용자 정보가 성공적으로 수정되었습니다.");
    setLoginMember(response.data.data);
    router.push("/user/me");
  }

  return (
    <>
      <div className="flex h-full w-full justify-center">
        <div className="flex flex-col gap-2">
          <h2 className="text-xl font-bold text-center">회원 정보 수정</h2>
          <form onSubmit={updateInfo} className="flex flex-col gap-2">
            <div>
              <label
                htmlFor="email"
                className="block text-sm font-medium text-gray-700"
              >
                {" "}
                이메일:
              </label>
              <Input
                type="email"
                name="email"
                placeholder="이메일"
                className="border-2 border-black"
                defaultValue={loginMember.email}
              />
            </div>
            <div>
              <label
                htmlFor="nickname"
                className="block text-sm font-medium text-gray-700"
              >
                {" "}
                닉네임:
              </label>
              <Input
                type="text"
                name="nickname"
                placeholder="닉네임"
                className="border-2 border-black"
                defaultValue={loginMember.nickname}
              />
            </div>
            <div>
              <label
                htmlFor="address"
                className="block text-sm font-medium text-gray-700"
              >
                주소:
              </label>
              <Input
                type="text"
                name="address"
                placeholder="주소"
                className="border-2 border-black w-[500px]"
                defaultValue={loginMember.address}
              />
            </div>
            <div>
              <label
                htmlFor="address"
                className="block text-sm font-medium text-gray-700"
              >
                프로필URL:
              </label>
              <Input
                type="url"
                name="profileUrl"
                placeholder="프로필URL"
                className="border-2 border-black w-[500px]"
                defaultValue={loginMember.profileUrl}
              />
            </div>

            <Button type="submit" className="bg-blue-500 text-white p-2 mt-4">
              수정
            </Button>
          </form>
        </div>
      </div>
    </>
  );
}
