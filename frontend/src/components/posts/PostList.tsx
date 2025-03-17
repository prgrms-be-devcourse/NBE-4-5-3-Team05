"use client";

import React from "react";
import Link from "next/link";
import Image from "next/image";
import type { components } from "@/lib/backend/apiV1/schema";

export type Post = components["schemas"]["PreviewPostResponse"];

interface PostListProps {
  posts: Post[];
}

export default function PostList({ posts }: PostListProps) {
  if (posts.length === 0) {
    return <div>등록된 게시글이 없습니다.</div>;
  }

  return (
    <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {posts.map((post) => {
        const thumbNailRaw = post.thumbNail?.trim();
        const thumbNail =
          thumbNailRaw && thumbNailRaw !== "null" ? thumbNailRaw : "";

        return (
          <li key={post.id} className="border rounded p-4 shadow">
            <Link href={`/posts/${post.id}`} className="block hover:opacity-90">
              {thumbNail ? (
                <div className="relative w-full h-40 mb-2 rounded">
                  <Image
                    src={thumbNail}
                    alt={post.title || "이미지"}
                    fill
                    className="object-cover rounded"
                  />
                </div>
              ) : (
                <div className="w-full h-40 bg-gray-200 flex items-center justify-center mb-2 rounded">
                  이미지 없음
                </div>
              )}
              <h3 className="font-bold text-lg">{post.title}</h3>
              <p className="mt-1">상품명: {post.productName}</p>
              <p className="mt-1">가격: {post.productPrice}원</p>
              <p className="mt-1 text-sm text-gray-500">
                작성자: {post.writerName}
              </p>
              <p className="mt-1 text-xs text-gray-400">
                {post.createdAt
                  ? new Date(post.createdAt).toLocaleDateString()
                  : ""}
              </p>
              <p className="mt-1 text-xs text-gray-400">
                조회수: {post.viewCount ?? 0}
              </p>
              <p className="mt-1 text-xs text-gray-400">
                찜: {post.likedCount ?? 0}
              </p>
            </Link>
          </li>
        );
      })}
    </ul>
  );
}
