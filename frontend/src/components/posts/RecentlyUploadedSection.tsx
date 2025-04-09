"use client";

import React, { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import type { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";

type PreviewPostResponse = components["schemas"]["PreviewPostResponse"];

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
  const [products, setProducts] = useState<PreviewPostResponse[]>([]);
  const [page, setPage] = useState<number>(1);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);
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

  useEffect(() => {
    async function fetchRecentlyUploaded() {
      try {
        setLoading(true);
        // 최신순(desc)으로 페이지(page) 단위, 10개씩 데이터를 가져옵니다.
        const res = await client.GET("/api/posts", {
          params: {
            query: {
              page: page,
              pageSize: 10,
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
      {products.length === 0 && !loading && !error && (
        <p>최근 업로드된 상품이 없습니다.</p>
      )}
      {error && <p className="text-red-500">{error}</p>}
      {/* grid-cols-5로 한 줄에 5개의 상품을 표시 */}
      <div className="grid grid-cols-5 gap-4">
        {products.map((product, index) => {
          // 마지막 요소에 observer ref 연결
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
                    // 이미지 크기를 2배 (h-48)로 설정
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
      {loading && <p>로딩 중...</p>}
    </div>
  );
}
