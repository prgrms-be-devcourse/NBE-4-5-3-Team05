"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import Link from "next/link";

export default function ClientPage({
  postInfo,
  pageInfo,
}: {
  postInfo: components["schemas"]["PreviewPostResponse"][];
  pageInfo: {
    totalPages: number;
    totalItems: number;
    currentPage: number;
    pageSize: number;
  };
}) {
  const [posts, setPosts] = useState(postInfo); // ✅ 기존 데이터 상태 저장
  const [currentPage, setCurrentPage] = useState(pageInfo.currentPage);
  const [isFetching, setIsFetching] = useState(false);
  const [hasMore, setHasMore] = useState(currentPage < pageInfo.totalPages);
  const observerRef = useRef<HTMLDivElement | null>(null);

  // ✅ 데이터 추가 로드 함수
  const fetchMorePosts = useCallback(async () => {
    if (isFetching || !hasMore) return;
    setIsFetching(true);

    try {
      const response = await client.GET("/api/posts/my/favorites", {
        params: {
          query: { page: currentPage + 1, pageSize: pageInfo.pageSize },
        },
        credentials: "include",
      });

      if (!response.error) {
        setPosts((prev) => [...prev, ...response.data?.data.items]);
        setCurrentPage((prev) => prev + 1);
        setHasMore(currentPage + 1 < response.data?.data.totalPages);
      }
    } catch (error) {
      console.error("❌ 찜한 게시물 추가 로드 실패:", error);
    } finally {
      setIsFetching(false);
    }
  }, [isFetching, currentPage, pageInfo.totalPages, hasMore]);

  // ✅ Intersection Observer 사용하여 무한 스크롤 감지
  useEffect(() => {
    if (!hasMore) return; // 🔥 마지막 페이지면 Observer 해제

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          fetchMorePosts();
        }
      },
      { threshold: 1.0 }
    );

    if (observerRef.current) observer.observe(observerRef.current);

    return () => observer.disconnect();
  }, [observerRef, currentPage, isFetching, hasMore]);

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold">찜한 게시물</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {posts.map((post) => (
          <div key={post.id} className="border p-4 rounded-md shadow-lg">
            <Link href={`/post/${post.id}`}>
              <img
                src={post.thumbNail}
                alt="썸네일 없음"
                className="w-full h-48 object-cover"
              />
              <h3 className="text-lg font-semibold mt-2">{post.title}</h3>
              <p className="text-gray-600">
                {post.productPrice?.toLocaleString()}원
              </p>
            </Link>
          </div>
        ))}
      </div>

      {/* 무한 스크롤 감지용 요소 */}
      <div ref={observerRef} className="h-10" />

      {/* 로딩 상태 표시 */}
      {isFetching && <p className="text-center">로딩 중...</p>}
      {!hasMore && (
        <p className="text-center text-gray-500">불러올 데이터가 없습니다.</p>
      )}
    </div>
  );
}
