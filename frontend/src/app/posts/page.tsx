"use client";

import React, { useEffect, useState } from "react";
import axios from "axios";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import Notice from "@/components/posts/Notice";
import FilterSidebar from "@/components/posts/FilterSiderbar";
import PostList from "@/components/posts/PostList";
import Pagination from "@/components/posts/Pagination";
import type { components } from "@/lib/backend/apiV1/schema";

type RsDataListNoticeResBody = components["schemas"]["RsDataListNoticeResBody"];
type NoticeResBody = components["schemas"]["NoticeResBody"];

type PageDtoPreviewPostResponse =
  components["schemas"]["PageDtoPreviewPostResponse"];
type RsDataPageDtoPreviewPostResponse =
  components["schemas"]["RsDataPageDtoPreviewPostResponse"];

type Category = components["schemas"]["Category"];

export default function PostsPage() {
  const searchParams = useSearchParams();
  const initialKeyword = searchParams.get("keyword") || "";

  const [noticeList, setNoticeList] = useState<NoticeResBody[]>([]);
  const [posts, setPosts] = useState<
    components["schemas"]["PreviewPostResponse"][]
  >([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [categories, setCategories] = useState<Category[]>([]);
  const [filters, setFilters] = useState({
    keyword: initialKeyword,
    minPrice: null as number | null,
    maxPrice: null as number | null,
    category: "",
    sort: "desc",
  });

  // 공지사항 로드
  useEffect(() => {
    (async () => {
      try {
        // /api/admin/notices/latest 엔드포인트가 리스트 형태로 공지사항을 반환한다고 가정합니다.
        const res = await axios.get<RsDataListNoticeResBody>(
          "/api/admin/notices/latest",
          {
            withCredentials: true,
          }
        );
        setNoticeList(res.data.data);
      } catch (err) {
        console.error("공지사항 로드 실패", err);
      }
    })();
  }, []);

  // 카테고리 로드
  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get<{
          code: string;
          message: string;
          data: Category[];
        }>("/api/categories", {
          withCredentials: true,
        });
        setCategories(res.data.data);
      } catch (err) {
        console.error("카테고리 로드 실패", err);
      }
    })();
  }, []);

  // 게시글 로드: 필터 상태나 페이지가 변경되면 호출
  const fetchPosts = async () => {
    try {
      const res = await axios.get<RsDataPageDtoPreviewPostResponse>(
        "/api/posts",
        {
          params: {
            page: currentPage,
            pageSize: 10,
            keyword: filters.keyword || undefined,
            sort: filters.sort,
            minPrice: filters.minPrice || undefined,
            maxPrice: filters.maxPrice || undefined,
            category: filters.category || undefined,
          },
          withCredentials: true,
        }
      );
      const { items, totalPages } = res.data.data;
      setPosts(items);
      setTotalPages(totalPages);
    } catch (err) {
      console.error("게시글 로드 실패", err);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, [currentPage, filters]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  return (
    <>
      <div className="flex space-x-4">
        <div className="flex-grow">
          {noticeList.length > 0 && (
            <div className="mb-4 space-y-4">
              {noticeList.map((notice, index) => (
                <Notice
                  key={index}
                  title={notice.title ?? ""}
                  content={notice.content ?? ""}
                />
              ))}
            </div>
          )}
          <h1 className="text-2xl font-bold mb-4">판매물품 리스트</h1>
          <PostList posts={posts} />
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </div>
        <FilterSidebar
          onFilterChange={(newFilters) => {
            setFilters(newFilters);
            setCurrentPage(1);
          }}
          categories={categories.map((cat) => ({
            id: cat.id ?? 0,
            name: cat.name ?? "",
          }))}
        />
      </div>
      <div className="fixed bottom-4 right-4">
        <Link href="/posts/new">
          <button className="px-4 py-2 bg-blue-600 text-white rounded shadow-lg">
            작성하기
          </button>
        </Link>
      </div>
    </>
  );
}
