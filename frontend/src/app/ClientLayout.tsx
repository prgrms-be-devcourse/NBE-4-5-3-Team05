"use client";

import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/backend/client";
import { faBookBookmark, faThumbsUp } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Geist, Geist_Mono } from "next/font/google";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Button } from "@/components/ui/button";


const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export default function ClinetLayout({
  children,
  me,
  fontVariable,
  fontClassName,
}: Readonly<{
  children: React.ReactNode;
  me:components["schemas"]["UserDto"];
  fontVariable: string;
  fontClassName: string;
}>) {
  const router = useRouter();
  const isLogined = me.id !== "";

  return (
    <html lang="en" className={`${fontVariable}`}>
      <body
        className={`min-h-[100dvh] flex flex-col ${fontClassName}`}>
          <FontAwesomeIcon
           icon={faThumbsUp}
           className="fa-fw text-4xl text-[red]"
         />
         <FontAwesomeIcon icon={faBookBookmark} />
        <header className="flex justify-end gap-3 px-4">
          <DropdownMenu >
            <DropdownMenuTrigger asChild>
              <Button variant="outline" className="relative z-10 bg-black text-white rounded-md">Menu</Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="absolute z-50 bg-white border border-gray-300 shadow-lg rounded-md right-0">
              <DropdownMenuLabel>{me.nickname}</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem>
                <Link href="/">메인</Link>
              </DropdownMenuItem>
              {isLogined && 
              <DropdownMenuItem>
                <Link href="/post">게시판</Link>
              </DropdownMenuItem>}
              {isLogined && 
              <DropdownMenuItem>
                <Link href="/member/me">내정보</Link>
              </DropdownMenuItem>}
              {!isLogined && 
              <DropdownMenuItem>
                <Link href="/member/login">로그인</Link>
              </DropdownMenuItem>}
              {isLogined && 
              <DropdownMenuItem>
                <Link href="/chat">채팅방</Link>
              </DropdownMenuItem>}
              {isLogined && 
              <DropdownMenuItem> 
                <Link href="" onClick={async (e)=>{
                e.preventDefault();
                const response=await client.POST("/api/users/logout",{
                  credentials:"include",
                });
                if(response.error){
                  alert(response.error.message);
                  return;
                }
                router.push("/");
                }}>로그아웃</Link>
              </DropdownMenuItem>}
            </DropdownMenuContent>
          </DropdownMenu>
                    
        </header>
        <div>
          {children}
        </div>
      </body>
    </html>
  );
}