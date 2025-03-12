import { RequestCookie } from "next/dist/compiled/@edge-runtime/cookies";
import { cookies } from "next/headers";
import createClient from "openapi-fetch";
import { NextRequest, NextResponse } from "next/server";
import client from "./lib/client";
import { parseAccessToken } from "./app/util/auth";

export async function middleware(request: NextRequest) {
  const myCookie = await cookies();
  const { isLogin, isExpired } = parseAccessToken(myCookie.get("accessToken"));
  const refresTokenCookie = myCookie.get("refreshToken");

  if (request.nextUrl.pathname.startsWith("/user/me") && !isLogin) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  if (isLogin && isExpired) {
    return refreshAccessToken(refresTokenCookie);
  }

  if (!isLogin && isProtectedRoute(request.nextUrl.pathname)) {
    return createUnauthorizedResponse();
  }
}

async function refreshAccessToken(refreshToken?: RequestCookie) {
  /* TODO : 리프레시 설정 후 set-cookie 설정 필요 */

  if (!refreshToken) {
    return createUnauthorizedResponse();
  }

  const response = await client.POST("/api/users/refresh", {
    body: { refreshToken: refreshToken.value },
    credentials: "include",
  });

  const rsData = response.data;

  const newAccessToken = rsData?.data;
  const nextResponse = NextResponse.next();
  nextResponse.headers.append(
    "Set-Cookie",
    `accessToken=${newAccessToken}; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=3600`
  );

  return nextResponse;
}

function createUnauthorizedResponse() {
  return new NextResponse("로그인이 필요합니다.", {
    status: 401,
    headers: {
      "Content-Type": "text/html; charset=utf-8",
    },
  });
}

function isProtectedRoute(pathname: string) {
  return (
    pathname.startsWith("/post/write") || pathname.startsWith("/post/edit")
  );
}

// middleware 지날 곳을 향후에 더 추가
export const config = {
  matcher: ["/user/me/:path*"], // `/user/me` 및 모든 하위 경로에 적용
};
