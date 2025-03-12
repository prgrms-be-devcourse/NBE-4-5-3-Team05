"use client";

import React, { useEffect, useState } from "react";
import axios from "axios";
import { useParams, useRouter } from "next/navigation";
import type { components } from "@/lib/backend/apiV1/schema";

// OpenAPI 스키마에서 생성된 타입을 사용
type ProductPostResponse = components["schemas"]["ProductPostResponse"];
type RsDataProductPostResponse = {
  code: string;
  message: string;
  data: ProductPostResponse;
};

export default function PostDetailPage() {
  const { postId } = useParams<{ postId: string }>(); // URL: /posts/[postId]
  const router = useRouter();

  const [post, setPost] = useState<ProductPostResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    if (!postId) return;

    async function fetchPost() {
      try {
        const response = await axios.get<RsDataProductPostResponse>(
          `/api/posts/${postId}`,
          {
            withCredentials: true,
          }
        );
        setPost(response.data.data);
        setLoading(false);
      } catch (err) {
        console.error("게시글 상세 조회 실패", err);
        setError("게시글 정보를 불러올 수 없습니다.");
        setLoading(false);
      }
    }

    fetchPost();
  }, [postId]);

  if (loading) {
    return <div className="p-4">로딩 중...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-500">{error}</div>;
  }

  if (!post) {
    return <div className="p-4">게시글 정보를 찾을 수 없습니다.</div>;
  }

  // imageUrls는 쉼표로 구분된 문자열로 전송된다고 가정
  const images = post.imageUrls ? post.imageUrls.split(",") : [];

  return (
    <div className="p-4">
      {/* 배너 영역 */}
      <div className="bg-gray-800 text-white p-4 rounded mb-4">
        <h1 className="text-2xl font-bold">
          배너 영역 (예: 사이트명, 로고 등)
        </h1>
      </div>

      {/* 상단 레이아웃: 사진 + 유저 정보 */}
      <div className="flex flex-col md:flex-row gap-4">
        {/* 사진 영역 */}
        <div className="flex-1 bg-gray-100 rounded p-4">
          <h2 className="text-xl font-semibold mb-2">대표 사진</h2>
          {images.length > 0 ? (
            <img
              src={images[0]}
              alt={post.title || "이미지"}
              className="w-full h-auto object-cover rounded"
            />
          ) : (
            <div className="h-64 flex items-center justify-center text-gray-500">
              이미지가 없습니다.
            </div>
          )}
        </div>

        {/* 유저 정보 + 판매자 정보 */}
        <div className="w-full md:w-1/3 bg-blue-50 rounded p-4">
          <h2 className="text-xl font-semibold mb-2">유저 정보</h2>
          <ul className="space-y-1">
            <li>작성자 ID: {post.writerId}</li>
            <li>작성자 닉네임: {post.writerName}</li>
          </ul>
          <div className="mt-4">
            {/* 채팅 걸기 버튼 - 로그인하지 않아도 상세조회는 공개이지만 채팅은 별도 */}
            <button
              onClick={() => router.push(`/chat/${post.id}`)}
              className="px-4 py-2 bg-blue-500 text-white rounded"
            >
              채팅 걸기
            </button>
          </div>
        </div>
      </div>

      {/* 중간 레이아웃: 판매글 정보 */}
      <div className="mt-4 bg-white rounded p-4 shadow">
        <div className="flex flex-col gap-2 md:flex-row md:justify-between md:items-center">
          <h2 className="text-2xl font-bold">{post.title}</h2>
          <div className="text-gray-500 text-sm">
            <span className="mr-2">
              작성일:{" "}
              {post.createdAt
                ? new Date(post.createdAt).toLocaleDateString()
                : ""}
            </span>
            {/* 필요하다면 조회수, 찜 횟수, 상태 등 추가 (스키마에 없으면 제외) */}
          </div>
        </div>
        <hr className="my-2" />
        <div className="flex flex-col gap-2 md:flex-row md:gap-8">
          <div>
            <strong>카테고리:</strong>{" "}
            {post.categories && post.categories.length > 0
              ? post.categories.join(", ")
              : "없음"}
          </div>
          <div>
            <strong>가격:</strong> {post.productPrice}원
          </div>
          <div>
            <strong>위치:</strong> 위도 {post.latitude}, 경도 {post.longitude}
          </div>
        </div>
        <hr className="my-2" />
        <div>
          <h3 className="text-lg font-semibold mb-2">본문 내용</h3>
          <p>{post.content}</p>
        </div>
        {images.length > 1 && (
          <div className="mt-4">
            <h3 className="text-lg font-semibold mb-2">추가 사진</h3>
            <div className="flex gap-2 overflow-x-auto">
              {images.slice(1).map((imgUrl, idx) => (
                <img
                  key={idx}
                  src={imgUrl}
                  alt={`${post.title} - ${idx + 1}`}
                  className="w-40 h-auto object-cover rounded"
                />
              ))}
            </div>
          </div>
        )}
      </div>

      {/* 댓글 영역은 스키마에 포함되어 있지 않아 생략 (필요 시 별도 API 호출) */}
    </div>
  );
}
