"use client";

import { useRouter } from "next/navigation";
import { use, useEffect } from "react";
import { Button } from "@/components/ui/button";
import client from "@/lib/client";
import Link from "next/link";
import { FaStore } from "react-icons/fa";
import {
  LoginMemberContext,
  useLoginMember,
} from "./stores/auth/loginMemberStore";
import { ToastContainer, toast } from "react-toastify";

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

  // 로그인 상태가 변경될 때마다 콘솔에 출력
  useEffect(() => {
    console.log("LoginMemberContext 업데이트:", loginMember);
  }, [loginMember]);

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

  useEffect(() => {
    // 브라우저 환경 체크 (Next.js에서는 SSR 시 window가 없음)
    if (typeof window !== "undefined") {
      console.log("SSE 연결 시작");
      const eventSource = new EventSource(
        "http://localhost:8080/api/notification/subscribe",
        {
          withCredentials: true,
        }
      );
      // 서버의 SSE 엔드포인트 (예시)

      eventSource.addEventListener("kafka-event", (event) => {
        console.log("event:", event.data);
      });
      eventSource.onmessage = (event) => {
        // event.data 는 서버에서 보내준 메시지 (문자열)
        console.log("SSE message received:", event.data);

        // 토스트로 표시
        toast.info(`새 메시지: ${event.data}`);
      };

      eventSource.onerror = (err) => {
        console.error("SSE error:", err);
        // 문제가 발생하면 연결 닫기
        eventSource.close();
      };

      // 컴포넌트 unmount 시에는 반드시 연결 종료
      return () => {
        eventSource.close();
      };
    }
  }, []);

  return (
    <LoginMemberContext.Provider value={loginMemberContextValue}>
      <div>
        <h1>Next.js SSE Demo</h1>
        <p>SSE로 들어오는 메시지를 토스트로 표시</p>
        {/* 토스트 표시 컨테이너 */}
        <ToastContainer />
      </div>
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
