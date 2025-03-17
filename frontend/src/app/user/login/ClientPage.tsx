"use client";

import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { use } from "react";

export default function ClientPage() {
  const router = useRouter();
  const { setLoginMember } = use(LoginMemberContext);

  let baseUrl = `${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_BACKEND_HOST}`;
  if (`${process.env.NEXT_PUBLIC_PROTOCOL}` === "https") {
    baseUrl += `/oauth2/authorization/kakao?redirectUrl=${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_FRONTEND_HOST}`;
  } else {
    baseUrl += `:${process.env.NEXT_PUBLIC_BACKEND_PORT}/oauth2/authorization/kakao?redirectUrl=${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_FRONTEND_HOST}:${process.env.NEXT_PUBLIC_FRONTEND_PORT}`;
  }

  async function login(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = e.target as HTMLFormElement;

    const username = form.username.value;
    const password = form.password.value;

    if (username.trim().length === 0) {
      alert("아이디를 입력해주세요.");
      return;
    }

    if (password.trim().length === 0) {
      alert("비밀번호를 입력해주세요.");
      return;
    }

    const response = await client.POST("/api/users/login", {
      body: {
        username,
        password,
      },
      credentials: "include",
    });

    console.log(response);

    if (response.error) {
      alert(response.error.message);
      return;
    }

    alert("로그인 성공하셨습니다.");
    if (response.data.data.item) {
      setLoginMember(response.data.data.item);
    }
    router.replace("/");
  }

  return (
    <>
      <div className="flex items-center justify-center h-screen">
        <div className="flex flex-col gap-2">
          <form onSubmit={login} className="flex flex-col w-full gap-2">
            <Input
              type="text"
              name="username"
              placeholder="아이디"
              className="border-2 border-black w-full"
            />
            <Input
              type="password"
              name="password"
              placeholder="비밀번호"
              className="border-2 border-black w-full"
            />
            <Button type="submit" className="cursor-pointer">
              로그인
            </Button>
          </form>
          <Link href="/user/signup">
            <Button className="w-full cursor-pointer">회원가입</Button>
          </Link>
          <Button variant="ghost" className="w-full mt-4 p-0">
            <Link href={baseUrl} className="w-full">
              <img src="/kakao_login.png" className="w-full" />
            </Link>
          </Button>
        </div>
      </div>
    </>
  );
}
