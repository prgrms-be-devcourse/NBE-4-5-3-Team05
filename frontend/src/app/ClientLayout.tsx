"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import client from "@/lib/client";
import Link from "next/link";
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
        {/* 로그인 여부에 따른 버튼 렌더링 */}
        <div className="flex gap-2">
          {/* 로그인 안한 경우 */}
          {!isLogin && (
            <Link href="/user/login">
              <Button>로그인 및 회원가입</Button>
            </Link>
          )}

          {/* 로그인한 경우 */}
          {isLogin && (
            <div className="flex items-center gap-2">
              {/* 관리자 계정일 때 */}
              {isAdmin && (
                <Link href="/admin">
                  <Button>관리자 페이지</Button>
                </Link>
              )}

              {/* 일반 유저 */}
              {!isAdmin && (
                <Link href="/user/me">
                  <Button>내 정보</Button>
                </Link>
              )}

              {/* 로그아웃 버튼 (공통) */}
              <Button className="cursor-pointer" onClick={handleLogout}>
                로그아웃
              </Button>
            </div>
          )}
        </div>
      </header>
      <div className="flex flex-1 flex-col items-center w-full">{children}</div>
      <footer>푸터</footer>
    </LoginMemberContext.Provider>
  );
}
