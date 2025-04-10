"use client";

import React, { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import type { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";

type PreviewPostResponse = components["schemas"]["PreviewPostResponse"];

/** 숫자형 가격을 '8,100원' 형식으로 포맷 */
function formatPrice(price: number): string {
  return (
    new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 0 }).format(price) +
    "원"
  );
}

/** 현재 시간과 등록 시간을 비교하여 경과 시간을 반환 */
function getRelativeTime(dateString: string): string {
  const now = new Date();
  const past = new Date(dateString);
  const diff = now.getTime() - past.getTime();
  const minute = 1000 * 60;
  const hour = minute * 60;
  const day = hour * 24;
  if (diff < minute) return "방금 전";
  if (diff < hour) {
    const minutes = Math.floor(diff / minute);
    return `${minutes}분 전`;
  }
  if (diff < day) {
    const hours = Math.floor(diff / hour);
    return `${hours}시간 전`;
  }
  const days = Math.floor(diff / day);
  return `${days}일 전`;
}

export default function RecentlyUploadedSection() {
  const router = useRouter();
  const [loading, setLoading] = useState<boolean>(true);
  const [products, setProducts] = useState<PreviewPostResponse[]>([]);
  const [page, setPage] = useState<number>(1);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  const observer = useRef<IntersectionObserver | null>(null);

  const lastProductElementRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (loading) return;
      if (observer.current) observer.current.disconnect();
      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          setPage((prevPage) => prevPage + 1);
        }
      });
      if (node) observer.current.observe(node);
    },
    [loading, hasMore]
  );

  // 페이지 번호 변경 시 데이터를 불러옴
  useEffect(() => {
    async function fetchRecentlyUploaded() {
      try {
        setLoading(true);
        const res = await client.GET("/api/posts", {
          params: {
            query: {
              page: page,
              pageSize: 10, // 한 페이지 당 가져올 항목 수
              sort: "desc",
            },
          },
          credentials: "include",
        });
        const { items } = res.data!.data;
        setProducts((prev) => [...prev, ...items]);
        if (items.length < 10) {
          setHasMore(false);
        }
      } catch (err) {
        console.error("최근 업로드 상품 조회 실패", err);
        setError("최근 업로드 상품을 불러오는데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    }
    fetchRecentlyUploaded();
  }, [page]);

  return (
    <div>
      <h2 className="font-bold text-lg mb-2">🔥 최근 올라온 상품</h2>
      {error && <p className="text-red-500">{error}</p>}
      {/* 초기 로딩 상태일 때만 로딩 메시지를 보여줍니다. */}
      {loading && products.length === 0 && (
        <div className="mt-4 text-center">
          <p className="animate-pulse">로딩 중...</p>
        </div>
      )}
      {/* 로딩이 끝났고, 상품이 없을 경우에만 "없음" 메시지를 출력 */}
      {!loading && products.length === 0 && !error && (
        <p className="mt-4 text-center text-gray-500">
          최근 업로드된 상품이 없습니다.
        </p>
      )}
      <div className="grid grid-cols-5 gap-4">
        {products.map((product, index) => {
          if (products.length === index + 1) {
            return (
              <div
                key={product.id}
                ref={lastProductElementRef}
                className="border p-2 text-center cursor-pointer"
                onClick={() => router.push(`/posts/${product.id}`)}
              >
                {product.thumbNail ? (
                  <img
                    src={product.thumbNail}
                    alt={product.title || "상품 이미지"}
                    className="w-full h-48 object-cover mb-2 rounded"
                  />
                ) : (
                  <div className="w-full h-48 bg-gray-200 flex items-center justify-center mb-2">
                    No Image
                  </div>
                )}
                <p className="text-sm font-medium">{product.title}</p>
                <p className="text-xs text-gray-600">
                  {formatPrice(product.productPrice)}&nbsp;&nbsp;
                  {getRelativeTime(product.createdAt)}
                </p>
              </div>
            );
          } else {
            return (
              <div
                key={product.id}
                className="border p-2 text-center cursor-pointer"
                onClick={() => router.push(`/posts/${product.id}`)}
              >
                {product.thumbNail ? (
                  <img
                    src={product.thumbNail}
                    alt={product.title || "상품 이미지"}
                    className="w-full h-48 object-cover mb-2 rounded"
                  />
                ) : (
                  <div className="w-full h-48 bg-gray-200 flex items-center justify-center mb-2">
                    No Image
                  </div>
                )}
                <p className="text-sm font-medium">{product.title}</p>
                <p className="text-xs text-gray-600">
                  {formatPrice(product.productPrice)}&nbsp;&nbsp;
                  {getRelativeTime(product.createdAt)}
                </p>
              </div>
            );
          }
        })}
      </div>
      {!hasMore && !loading && (
        <p className="mt-4 text-center text-gray-500">
          더 이상 불러올 게시글이 없습니다.
        </p>
      )}
    </div>
  );
}
