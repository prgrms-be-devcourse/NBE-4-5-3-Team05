import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";
import client from "./lib/client";
import { parseAccessToken } from "./app/util/auth";

export async function middleware(request: NextRequest) {
  const myCookie = await cookies();
  const accessTokenCookie = myCookie.get("accessToken");

  const { isLogin, isExpired } = parseAccessToken(accessTokenCookie);

  // ✅ 2. 로그인 상태이고, AccessToken이 유효하면 패스
  if (isLogin && !isExpired) {
    console.log("✅ AccessToken 유효 → 요청 패스");
    return NextResponse.next();
  }

  // ✅ 3. 로그인 상태인데 만료됨 → RefreshToken 존재 여부 확인
  if (isLogin && isExpired) {
    console.log("🔄 AccessToken 만료 → RefreshToken으로 재발급 시도");
    return refreshAccessToken();
  }
}

// ❌ 강제 로그아웃 + 쿠키 삭제 + 로그인 화면 이동 (헤더 UI 반영)
function forceLogout(request: NextRequest, redirectUrl: string = "/") {
  console.log("🔴 강제 로그아웃 실행");

  const nextResponse = NextResponse.redirect(new URL(redirectUrl, request.url));

  // 🟢 AccessToken 삭제
  nextResponse.headers.append(
    "Set-Cookie",
    `accessToken=; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=0`
  );

  return nextResponse;
}

// 🔄 RefreshToken을 사용해 AccessToken 재발급
async function refreshAccessToken() {
  const nextResponse = NextResponse.next();

  const response = await client.GET("/api/users/me", {
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  // ✅ 새로운 AccessToken 쿠키 저장 (Max-Age 추가)
  const springCookie = response.response.headers.getSetCookie();
  console.log("스프링 쿠키:" + springCookie);
  nextResponse.headers.set("set-cookie", String(springCookie));

  // 🗑 RefreshToken 삭제 (1회용)
  // nextResponse.headers.append(
  //   "Set-Cookie",
  //   `refreshToken=; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=0`
  // );

  return nextResponse;
}

// middleware 지날 곳을 향후에 더 추가
export const config = {
  matcher: ["/user/me/:path*"], // `/user/me` 및 모든 하위 경로에 적용
};
