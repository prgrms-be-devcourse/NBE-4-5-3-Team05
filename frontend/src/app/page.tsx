"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";


export default function Page() {
  return (
    <div>
      <button>
        <div>
        <a href="http://localhost:8080/oauth2/authorization/kakao?redirectUrl=localhost:3000">
          카카오 로그인
        </a>  
        </div>
      </button>
    </div>
  );
}
