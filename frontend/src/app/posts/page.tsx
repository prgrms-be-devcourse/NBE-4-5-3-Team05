"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import Notice from "@/components/posts/Notice";
import FilterSidebar from "@/components/posts/FilterSiderbar";
import PostList from "@/components/posts/PostList";
import Pagination from "@/components/posts/Pagination";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";

type NoticeListItem = components["schemas"]["NoticeResBody"];
type ProductPostListItem = components["schemas"]["PreviewPostResponse"];

export default function PostsPage() {
  const [noticeList, setNoticeList] = useState<NoticeListItem[]>([]);
  const [posts, setPosts] = useState<ProductPostListItem[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [categories, setCategories] = useState<{ id: number; name: string }[]>(
    []
  );
  const [filters, setFilters] = useState({
    keyword: "",
    minPrice: null as number | null,
    maxPrice: null as number | null,
    categories: [] as string[],
    sort: "desc",
  });

  // 공지사항 불러오기
  useEffect(() => {
    (async () => {
      const res = await client.GET("/api/admin/notices/latest", {
        Credential: "include",
      });
      if (res.error) {
        console.error("공지사항 로드 실패", res.error);
        return;
      }
      setNoticeList(res.data!.data);
    })();
  }, []);

  // 카테고리 목록 불러오기
  useEffect(() => {
    (async () => {
      const res = await client.GET("/api/categories", {
        Credential: "include",
      });
      if (res.error) {
        console.error("카테고리 로드 실패", res.error);
        return;
      }
      const cats = res.data!.data.map((cat: any) => ({
        id: cat.id ?? 0,
        name: cat.name ?? "",
      }));
      setCategories(cats);
    })();
  }, []);

  // 게시글 불러오기 (필터, 페이지 변경 시)
  const fetchPosts = async () => {
    const res = await client.GET("/api/posts", {
      params: {
        query: {
          page: currentPage,
          pageSize: 10,
          keyword: filters.keyword || undefined,
          sort: filters.sort,
          minPrice: filters.minPrice || undefined,
          maxPrice: filters.maxPrice || undefined,
          // 다중 카테고리인 경우 콤마로 구분하여 전달
          categories: filters.categories.length
            ? filters.categories.join(",")
            : undefined,
        },
      },
      credentials: "include",
    });
    if (res.error) {
      console.error("게시글 로드 실패", res.error);
      return;
    }
    const { items, totalPages } = res.data!.data;
    setPosts(items);
    setTotalPages(totalPages);
  };

  useEffect(() => {
    fetchPosts();
  }, [currentPage, filters]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  return (
    <>
      <div className="flex w-full space-x-4">
        <div className="flex-grow">
          {noticeList.length > 0 && (
            <div className="mb-4 space-y-4">
              {noticeList.map((notice, index) => (
                <Notice
                  key={index}
                  title={notice.title!}
                  content={notice.content!}
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
          categories={categories}
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
