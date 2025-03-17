"use client";

import React, { use, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import Link from "next/link";
import RecentlyViewedSection from "@/components/posts/RecentlyViewedSection";
import RecentlyUploadedSection from "@/components/posts/RecentlyUploadedSection";
import { LoginMemberContext } from "./stores/auth/loginMemberStore";
import client from "@/lib/client";
import { components } from "@/lib/backend/apiV1/schema";

type Notice = components["schemas"]["NoticeResBody"];
type Category = {
  id: number;
  name: string;
};

export default function ClientPage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const router = useRouter();
  const { isLogin, isAdmin } = use(LoginMemberContext);

  // notice
  const [isOpen, setIsOpen] = useState(false); // 공지사항 펼치기/접기 상태
  const [notices, setNotices] = useState<Notice[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  const handleSearch = () => {
    router.push(`/posts?keyword=${encodeURIComponent(searchKeyword)}`);
  };

  // 최신 공지사항 5개 불러오기
  useEffect(() => {
    if (!isOpen) return; // 공지사항이 열릴 때만 API 호출

    async function fetchNotices() {
      try {
        const response = await client.GET("/api/admin/notices/latest");

        if (response.error) {
          throw new Error(response.error.message);
        }

        setNotices(response.data.data || []);
      } catch (err) {
        console.error("❌ 공지사항 불러오기 실패:", err);
        setError("공지사항을 불러오는 중 오류가 발생했습니다.");
      } finally {
        setIsLoading(false);
      }
    }

    fetchNotices();
  }, [isOpen]);

  // 공지사항 토글 함수
  const handleToggleNotices = () => {
    setIsOpen((prev) => !prev);
  };

  return (
    <div className="flex flex-col gap-4 p-4 w-full">
      {/* 상단 섹션 */}
      <section className="flex items-start gap-4 border p-4">
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
          {/* 카테고리 필터 UI (추후 동적 구현 가능) */}
          {/* {
            <div className="mt-4">
              <h3 className="font-bold mb-2">📌 카테고리 필터</h3>
              
              <label className="flex items-center space-x-2 border p-2 rounded-md cursor-pointer">
                <input type="checkbox" />
                <span>카테고리</span>
              </label>
            </div>
          } */}
        </div>
        <div className="flex gap-2">
          <Link href="/posts">
            <Button variant="default">판매하기</Button>
          </Link>
          <Link href="/user/me/sell/manage">
            <Button variant="default">내 상점</Button>
          </Link>
          <Button variant="outline">💬 채팅</Button>
        </div>
      </section>

      {/* 공지사항 섹션 */}
      <section className="border p-4">
        {/* 공지사항 버튼 */}
        <Button
          className="flex justify-start text-lg font-bold"
          onClick={handleToggleNotices}
        >
          📢 공지사항
        </Button>

        {/* 공지사항 목록 (토글 상태일 때만 표시) */}
        {isOpen && (
          <div className="mt-2">
            {isLoading ? (
              <p className="text-gray-500">📡 공지사항을 불러오는 중...</p>
            ) : error ? (
              <p className="text-red-500">{error}</p>
            ) : notices.length === 0 ? (
              <p className="text-gray-500">
                📭 현재 등록된 공지사항이 없습니다.
              </p>
            ) : (
              <ul className="list-disc pl-5 space-y-2">
                {notices.map((notice) => (
                  <li key={notice.id} className="text-sm text-gray-700">
                    <Link
                      href={`/notices/${notice.id}`}
                      className="hover:underline"
                    >
                      <strong>{notice.title}</strong>
                    </Link>
                    <span className="ml-2 text-xs text-gray-500">
                      - {notice.admin?.nickname} (
                      {new Date(notice.createdAt ?? "").toLocaleDateString()})
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </section>

      {/* 최근 본 상품 섹션 */}
      <section className="border p-4">
        <Button className="flex justify-start text-lg font-bold">
          최근 본 상품
        </Button>
        <RecentlyViewedSection isLogin={isLogin} />
      </section>

      {/* 최근 올라온 상품 섹션 */}
      <section className="border p-4">
        <RecentlyUploadedSection />
      </section>
    </div>
  );
}
