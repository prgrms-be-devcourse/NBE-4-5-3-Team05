"use client";

import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import Link from "next/link";
import RecentlyViewedSection from "@/components/posts/RecentlyViewedSection";
import RecentlyUploadedSection from "@/components/posts/RecentlyUploadedSection";
import { LoginMemberContext } from "./stores/auth/loginMemberStore";
import client from "@/lib/client";
import { components } from "@/lib/backend/apiV1/schema";
import { faComment, faBars } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import ScrollToTopButton from "@/components/posts/ScrollToTopButton";

// API 응답으로 받아올 카테고리 타입
type Category = {
  id: number;
  name: string;
};

type Notice = components["schemas"]["NoticeResBody"];

export default function ClientPage() {
  const [searchKeyword, setSearchKeyword] = useState("");
  const router = useRouter();
  const { isLogin, isAdmin } = React.useContext(LoginMemberContext);

  // notice 관련 상태
  const [isOpen, setIsOpen] = useState(false); // 공지사항 펼치기/접기 상태
  const [notices, setNotices] = useState<Notice[]>([]);
  const [noticeLoading, setNoticeLoading] = useState<boolean>(true);
  const [noticeError, setNoticeError] = useState<string>("");
  const [categories, setCategories] = useState<Category[]>([]);
  const [catLoading, setCatLoading] = useState<boolean>(true);
  const [catError, setCatError] = useState<string>("");

  // 전체 카테고리 옵션 (전체 선택 시 별도의 필터 없이 이동)
  const displayedCategories: Category[] = [
    { id: 0, name: "전체 카테고리" },
    ...categories,
  ];

  const handleSearch = () => {
    router.push(`/posts?keyword=${encodeURIComponent(searchKeyword)}`);
  };

  const handleCategorySelect = (category: Category) => {
    if (category.id === 0) {
      router.push("/posts");
    } else {
      const queryParams = new URLSearchParams({
        categoryIds: category.id.toString(),
      });
      router.push(`/posts?${queryParams.toString()}`);
    }
  };

  // 최신 공지사항 5개 불러오기 (공지사항 섹션)
  useEffect(() => {
    if (!isOpen) return;
    async function fetchNotices() {
      try {
        const response = await client.GET("/api/admin/notices/latest");
        if (response.error) {
          throw new Error(response.error.message);
        }
        setNotices(response.data.data || []);
      } catch (err) {
        console.error("❌ 공지사항 불러오기 실패:", err);
        setNoticeError("공지사항을 불러오는 중 오류가 발생했습니다.");
      } finally {
        setNoticeLoading(false);
      }
    }
    fetchNotices();
  }, [isOpen]);

  // 카테고리 목록 API
  useEffect(() => {
    async function fetchCategories() {
      try {
        const res = await client.GET("/api/categories", {
          credentials: "include",
        });
        setCategories(res.data?.data || []);
      } catch (e) {
        console.error("카테고리 조회 실패:", e);
        setCatError("카테고리를 불러오는데 실패했습니다.");
      } finally {
        setCatLoading(false);
      }
    }
    fetchCategories();
  }, []);

  // 공지사항 토글 함수
  const handleToggleNotices = () => {
    setIsOpen((prev) => !prev);
  };

  const [dropdownVisible, setDropdownVisible] = useState(false);
  const columnCount = Math.ceil(displayedCategories.length / 10);

  return (
    <div className="max-w-7xl mx-auto px-6 py-6 w-full bg-gray-100 min-h-screen">
      {/* 상단 섹션 */}
      <section className="flex flex-col md:flex-row items-center gap-4 rounded-xl shadow-md p-6 bg-white mb-6">
        <div className="flex items-center gap-2 w-full">
          {/* 버튼과 드롭다운을 포함하는 부모 컨테이너 */}
          <div
            className="relative inline-block"
            onMouseEnter={() => setDropdownVisible(true)}
            onMouseLeave={() => setDropdownVisible(false)}
          >
            <button
              className="p-2 rounded-md border border-gray-300 focus:outline-none h-12 flex items-center justify-center"
              aria-label="카테고리 선택"
            >
              <FontAwesomeIcon icon={faBars} />
            </button>
            {dropdownVisible && (
              <div className="absolute left-0 top-full -mt-1 w-64 bg-white border border-gray-200 shadow-lg rounded-md z-10">
                {catLoading ? (
                  <p className="px-4 py-2 text-gray-500">로딩 중...</p>
                ) : catError ? (
                  <p className="px-4 py-2 text-red-500">{catError}</p>
                ) : (
                  <ul
                    style={{
                      gridTemplateColumns: `repeat(${columnCount}, minmax(0, 1fr))`,
                    }}
                    className="grid gap-1 text-sm p-2"
                  >
                    {displayedCategories.map((cat) => (
                      <li
                        key={cat.id}
                        onClick={() => {
                          handleCategorySelect(cat);
                          setDropdownVisible(false);
                        }}
                        className="px-2 py-1 hover:bg-gray-100 cursor-pointer"
                      >
                        {cat.name}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>
          {/* 검색 입력창 및 버튼 */}
          <div className="flex-grow">
            <div className="flex items-center gap-2">
              <input
                type="text"
                placeholder="상품명 검색"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                className="w-full p-3 h-12 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    handleSearch();
                  }
                }}
              />
              <Button onClick={handleSearch} className="px-6 h-12">
                검색
              </Button>
            </div>
          </div>
        </div>
        <div className="flex gap-2">
          <Link href="/posts">
            <Button variant="default" className="px-6 h-12">
              판매하기
            </Button>
          </Link>
          <Link href="/user/me/sell/manage">
            <Button variant="default" className="px-6 h-12">
              내 상점
            </Button>
          </Link>
          <Button variant="outline" asChild className="px-6 h-12">
            <Link href="/chat" className="flex items-center gap-2">
              <FontAwesomeIcon icon={faComment} />
              채팅방
            </Link>
          </Button>
        </div>
      </section>

      {/* 공지사항 섹션 */}
      <section className="rounded-xl shadow-md p-6 bg-white mb-6">
        <Button
          className="flex justify-start text-lg font-bold mb-4"
          onClick={handleToggleNotices}
        >
          📢 공지사항
        </Button>
        {isOpen && (
          <div>
            {noticeLoading ? (
              <p className="text-gray-500">📡 공지사항을 불러오는 중...</p>
            ) : noticeError ? (
              <p className="text-red-500">{noticeError}</p>
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
                      {new Date(notice.createdAt ?? "").toLocaleDateString()} )
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </section>

      {/* 최근 본 상품 섹션 */}
      <section className="rounded-xl shadow-md p-6 bg-white mb-6">
        <Button className="flex justify-start text-lg font-bold mb-4">
          최근 본 상품
        </Button>
        <RecentlyViewedSection isLogin={isLogin} />
      </section>

      {/* 최근 올라온 상품 섹션 */}
      <section className="rounded-xl shadow-md p-6 bg-white mb-6">
        <RecentlyUploadedSection />
      </section>

      {/* 우측 하단에 Top 버튼 */}
      <ScrollToTopButton />
    </div>
  );
}
