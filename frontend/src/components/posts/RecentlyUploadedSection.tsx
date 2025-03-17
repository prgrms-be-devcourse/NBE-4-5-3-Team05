"use client";

import React, { useState, useEffect } from "react";

import { useRouter } from "next/navigation";
import type { components } from "@/lib/backend/apiV1/schema";

type PreviewPostResponse = components["schemas"]["PreviewPostResponse"];

export default function RecentlyUploadedSection() {
  const router = useRouter();
  const [products, setProducts] = useState<PreviewPostResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    async function fetchRecentlyUploaded() {
      try {
        // 최근 올라온 상품은 최신순(desc)으로 1페이지, 10개 항목 불러온다고 가정합니다.
        const res = await client.GET<{
          code: string;
          message: string;
          data: components["schemas"]["PageDtoPreviewPostResponse"];
        }>("/api/posts", {
          params: {
            page: 1,
            pageSize: 10,
            sort: "desc",
          },
          withCredentials: true,
        });
        const { items } = res.data.data;
        setProducts(items);
      } catch (err) {
        console.error("최근 업로드 상품 조회 실패", err);
        setError("최근 업로드 상품을 불러오는데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    }
    fetchRecentlyUploaded();
  }, []);

  if (loading) return <p>로딩 중...</p>;
  if (error) return <p className="text-red-500">{error}</p>;
  if (products.length === 0) return <p>최근 업로드된 상품이 없습니다.</p>;

  return (
    <div>
      <h2 className="font-bold text-lg mb-2">🔥 최근 올라온 상품</h2>
      <div className="flex flex-wrap gap-4">
        {products.map((product) => (
          <div
            key={product.id}
            className="border p-2 min-w-[100px] text-center cursor-pointer"
            onClick={() => router.push(`/posts/${product.id}`)}
          >
            {product.thumbNail ? (
              <img
                src={product.thumbNail}
                alt={product.title || "상품 이미지"}
                className="w-full h-24 object-cover mb-2 rounded"
              />
            ) : (
              <div className="w-full h-24 bg-gray-200 flex items-center justify-center mb-2">
                No Image
              </div>
            )}
            <p className="text-sm">{product.title}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
