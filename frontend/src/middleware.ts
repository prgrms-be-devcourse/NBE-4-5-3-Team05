import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

export async function middleware(request: NextRequest){
    console.log("url:",request.url);
    const accessToken = request.cookies.get("accessToken");
    const refreshToken = request.cookies.get("refreshToken");
    console.log(accessToken, refreshToken)
    console.log(request.headers.getSetCookie());

    const response = await fetch("http://localhost:8080/api/users/me",{
        headers:{
            cookie: (await cookies()).toString(),
        }
    });
    console.log("response:", response.headers)



    const nextResponse = NextResponse.next();

    const springCookie = response.headers.getSetCookie();
    nextResponse.headers.set("set-cookie", String(springCookie));
    return nextResponse;
}