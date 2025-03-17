"use client";

import React, { use, useState } from "react";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import Link from "next/link";
import RecentlyViewedSection from "@/components/posts/RecentlyViewedSection";
import RecentlyUploadedSection from "@/components/posts/RecentlyUploadedSection";
import { LoginMemberContext } from "./stores/auth/loginMemberStore";

export default function ClientPage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const router = useRouter();
  const { isLogin, isAdmin } = use(LoginMemberContext);

  const handleSearch = () => {
    router.push(`/posts?keyword=${encodeURIComponent(searchKeyword)}`);
  };

  return (
    <div className="flex flex-col gap-4 p-4 w-full">
      {/* 상단 섹션 */}
      <section className="flex items-start gap-4 border p-4">
        <div className="flex flex-col">
          <span>📍 현재 위치</span>
          <Button variant="outline">위치 수정</Button>
        </div>
        <div className="flex-grow">
          <div className="flex gap-2">
            <input
              type="text"
              placeholder="상품명 검색"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              className="w-full p-2 border rounded-md"
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  handleSearch();
                }
              }}
            />
            <Button onClick={handleSearch}>검색</Button>
          </div>
          <div className="mt-4">
            <h3 className="font-bold mb-2">📌 카테고리 필터</h3>
            {/* 카테고리 필터 UI (추후 동적 구현 가능) */}
            <label className="flex items-center space-x-2 border p-2 rounded-md cursor-pointer">
              <input type="checkbox" />
              <span>카테고리</span>
            </label>
          </div>
        </div>
        <div className="flex gap-2">
          <Button variant="default">
            <Link href="/posts">판매하기</Link>
          </Button>
          <Button variant="default">
            <Link href="/user/me/sell/manage">내 상점</Link>
          </Button>
          <Button variant="outline">💬 채팅</Button>
        </div>
      </section>

      {/* 공지사항 섹션 (추후 동적 데이터 처리 예정) */}
      <section className="border p-4">
        <Button className="flex justify-start text-lg font-bold">
          📢 공지사항
        </Button>
      </section>

      {/* 최근 본 상품 섹션 */}
      <section className="border p-4">
        <RecentlyViewedSection isLogin={isLogin} />
      </section>

      {/* 최근 올라온 상품 섹션 */}
      <section className="border p-4">
        <RecentlyUploadedSection />
      </section>
    </div>
  );
}
