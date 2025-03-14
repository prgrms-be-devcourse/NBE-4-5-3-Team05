"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";

export default function ClientPage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const router = useRouter();

  const handleSearch = () => {
    // 검색어를 쿼리 파라미터로 전달하여 판매목록 리스트 페이지로 이동
    router.push(`/posts?keyword=${encodeURIComponent(searchKeyword)}`);
  };

  return (
    <div className="flex flex-col gap-4 p-4">
      {/* 상단 섹션 */}
      <section className="flex items-start gap-4 border p-4">
        {/* 왼쪽 위 : 현재 위치 및 위치 수정버튼 */}
        <div className="flex flex-col">
          <span>📍 현재 위치</span>
          <Button variant="outline">위치 수정</Button>
        </div>
        {/* 중앙: 검색 및 필터링 */}
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
            <label className="flex items-center space-x-2 border p-2 rounded-md cursor-pointer">
              <input type="checkbox" />
              <span>카테고리</span>
            </label>
          </div>
        </div>
        {/* 오른쪽 끝: 판매하기, 내 상점, 채팅 */}
        <div className="flex gap-2">
          <Button variant="default">판매하기</Button>
          <Button variant="default">내 상점</Button>
          <Button variant="outline">💬 채팅</Button>
        </div>
      </section>

      {/* 공지사항 및 최근 본 상품 */}
      <section className="border p-4">
        <Button className="flex justify-start text-lg font-bold">
          📢 공지사항
        </Button>
        {/* 공지사항 목록 구현 예정 */}
        <div>
          <h3 className="font-bold mb-2 text-lg">🛒 최근 본 상품</h3>
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
        </div>
      </section>

      {/* 최근 올라온 상품 - 무한 스크롤 */}
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
    </div>
  );
}
