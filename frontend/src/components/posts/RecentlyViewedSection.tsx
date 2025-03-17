"use client";

import React, { useEffect, useState } from "react";

import { useRouter } from "next/navigation";
import type { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";


type PreviewPostResponse = components["schemas"]["PreviewPostResponse"];

export default function RecentlyViewedSection() {
  const router = useRouter();
  const [isLogin, setIsLogin] = useState<boolean>(false);
  const [recentProducts, setRecentProducts] = useState<PreviewPostResponse[]>(
    []
  );
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  // 로그인 상태 체크
  useEffect(() => {
    async function checkLogin() {

      const result = await client.GET("/api/users/me", {
        credentials: "include",
      });
      if (result.error) {
        setIsLogin(false);
      }
      setIsLogin(true);

    }
    checkLogin();
  }, []);

  // 로그인한 경우에만 최근 본 상품 데이터를 불러옴
  useEffect(() => {
    if (!isLogin) {
      setLoading(false);
      return;
    }
    async function fetchRecentlyViewed() {
      try {

        const res = await client.GET("/api/posts/recently-viewed", {
          withCredentials: true,
        });
        setRecentProducts(res.data!.data);

      } catch (err) {
        console.error("최근 본 상품 조회 실패", err);
        setError("최근 본 상품을 불러오는데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    }
    fetchRecentlyViewed();
  }, [isLogin]);

  if (loading) return <p>로딩 중...</p>;

  if (!isLogin) {
    return <p>로그인을 먼저 해주세요.</p>;
  }

  if (error) return <p className="text-red-500">{error}</p>;

  if (recentProducts.length === 0) {
    return <p>최근 본 상품이 없습니다.</p>;
  }

  return (
    <div>
      <h3 className="font-bold mb-2 text-lg">최근 본 상품</h3>
      <div className="flex flex-wrap gap-4">
        {recentProducts.map((product) => (
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
