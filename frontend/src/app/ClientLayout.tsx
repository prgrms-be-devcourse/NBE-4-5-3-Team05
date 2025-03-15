"use client";

import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import { cookies } from "next/headers";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { FaStore } from "react-icons/fa";
import {
  LoginMemberContext,
  useLoginMember,
} from "./stores/auth/loginMemberStore";

export default function ClientLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const router = useRouter();
  const {
    setLoginMember,
    isLogin,
    loginMember,
    removeLoginMember,
    isLoginMemberPending,
    isAdmin,
    setNoLoginMember,
  } = useLoginMember();

  const loginMemberContextValue = {
    loginMember,
    setLoginMember,
    removeLoginMember,
    isLogin,
    isLoginMemberPending,
    isAdmin,
    setNoLoginMember,
  };

  async function handleLogout(e: React.MouseEvent<HTMLButtonElement>) {
    e.preventDefault();
    const response = await client.POST("/api/users/logout", {
      credentials: "include",
    });

    if (response.error) {
      alert("로그아웃 되었습니다.");
      removeLoginMember();
      router.replace("/user/login");
      return;
    }

    alert("로그아웃 되었습니다.");
    removeLoginMember();
    router.replace("/");
  }

  async function fetchLoginMember() {
    const response = await client.GET("/api/users/me", {
      credentials: "include",
    });

    if (response.error) {
      setNoLoginMember();
      return;
    }

    setLoginMember(response.data.data);
  }

  useEffect(() => {
    fetchLoginMember();
  }, []);

  return (
    <LoginMemberContext.Provider value={loginMemberContextValue}>
      <header className="flex justify-between">
        <Link href="/" className="flex items-center gap-2">
          <Button>
            <FaStore className="text-lg" />
            길게 볼 장터
          </Button>
        </Link>
        {isLogin && (
          <div className="flex items-center gap-2">
            <Link href="/user/me">
              <Button>내 정보</Button>
            </Link>
            <Button className="cursor-pointer" onClick={handleLogout}>
              로그아웃
            </Button>
          </div>
        )}
        {!isLogin && (
          <Link href="/user/login">
            <Button>로그인 및 회원가입</Button>
          </Link>
        )}
      </header>
      <div className="flex-grow">{children}</div>
      <footer>푸터</footer>
    </LoginMemberContext.Provider>
  );
}
