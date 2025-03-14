"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import Link from "next/link";
import RecentlyViewedSection from "@/components/posts/RecentlyViewedSection";

interface ClientPageProps {
  me: {
    id: string;
    nickname: string;
  };
}

export default function ClientPage({ me }: ClientPageProps) {
  const [searchKeyword, setSearchKeyword] = useState("");
  const router = useRouter();

  const handleSearch = () => {
    router.push(`/posts?keyword=${encodeURIComponent(searchKeyword)}`);
  };

  return (
    <div className="flex flex-col gap-4 p-4">
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
          <Button variant="default">판매하기</Button>
          <Button variant="default">내 상점</Button>
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
        <RecentlyViewedSection />
      </section>

      {/* 최근 올라온 상품 섹션 (하드코딩 예시) */}
      <section className="border p-4">
        <h2 className="font-bold text-lg mb-2">🔥 최근 올라온 상품</h2>
        <div className="flex flex-wrap gap-4">
          <div className="border p-2 min-w-[100px] text-center">상품 1</div>
          <div className="border p-2 min-w-[100px] text-center">상품 2</div>
          <div className="border p-2 min-w-[100px] text-center">상품 3</div>
          <div className="border p-2 min-w-[100px] text-center">상품 4</div>
          <div className="border p-2 min-w-[100px] text-center">상품 5</div>
          <div className="border p-2 min-w-[100px] text-center">상품 6</div>
          <div className="border p-2 min-w-[100px] text-center">상품 7</div>
          <div className="border p-2 min-w-[100px] text-center">상품 8</div>
          <div className="border p-2 min-w-[100px] text-center">상품 9</div>
          <div className="border p-2 min-w-[100px] text-center">상품 10</div>
        </div>
        <div className="h-10 flex items-center justify-center">
          <p className="text-gray-500">로딩 중...</p>
        </div>
      </section>

      {/* 하단 고정 버튼 */}
      <div className="fixed bottom-4 right-4">
        <Link href="/posts/new">
          <Button className="px-4 py-2 bg-blue-600 text-white rounded shadow-lg">
            작성하기
          </Button>
        </Link>
      </div>
    </div>
  );
}
