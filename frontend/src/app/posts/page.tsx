"use client";

import React, { useEffect, useState } from "react";
import axios from "axios";
import Link from "next/link";
import Notice from "@/components/posts/Notice";
import FilterSidebar from "@/components/posts/FilterSiderbar";
import PostList from "@/components/posts/PostList";
import Pagination from "@/components/posts/Pagination";

interface PostsResponseData {
  items: any[]; // 백엔드에서 받은 ProductPostResponse 배열
  totalPages: number;
  totalItems: number;
  curPageNo: number;
  pageSize: number;
}

interface RsData<T> {
  code: string;
  message: string;
  data: T;
}

interface NoticeData {
  title: string;
  content: string;
}

interface CategoryData {
  id?: number;
  name?: string;
}

export default function PostsPage() {
  const [noticeList, setNoticeList] = useState<NoticeData[]>([]);
  const [posts, setPosts] = useState<any[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [categories, setCategories] = useState<{ id: number; name: string }[]>(
    []
  );
  const [filters, setFilters] = useState({
    keyword: "",
    minPrice: null as number | null,
    maxPrice: null as number | null,
    category: "",
    sort: "desc",
  });

  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get<RsData<NoticeData[]>>(
          "/api/admin/notices/latest",
          { withCredentials: true }
        );
        setNoticeList(res.data.data);
      } catch (err) {
        console.error("공지사항 로드 실패", err);
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get<RsData<CategoryData[]>>("/api/categories", {
          withCredentials: true,
        });
        const cats = res.data.data.map((cat) => ({
          id: cat.id ?? 0,
          name: cat.name ?? "",
        }));
        setCategories(cats);
      } catch (err) {
        console.error("카테고리 로드 실패", err);
      }
    })();
  }, []);

  const fetchPosts = async () => {
    try {
      const res = await axios.get<RsData<PostsResponseData>>("/api/posts", {
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
      });
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
                  title={notice.title}
                  content={notice.content}
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
