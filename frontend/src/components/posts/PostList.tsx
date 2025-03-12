// components/PostList.tsx
"use client";

import React from "react";
import Link from "next/link";

export interface Post {
  id: string;
  productName: string;
  productPrice: number;
  title: string;
  writerId: string;
  writerName: string;
  latitude: number;
  longitude: number;
  thumbNail: string;
  createdAt: string; // ISO string 형태로 받는다고 가정
}

interface PostListProps {
  posts: Post[];
}

export default function PostList({ posts }: PostListProps) {
  if (posts.length === 0) {
    return <div>등록된 게시글이 없습니다.</div>;
  }

  return (
    <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {posts.map((post) => (
        <li key={post.id} className="border rounded p-4 shadow">
          {/* 
            Next.js 13 (App Router)에서는 <Link>를 감쌀 때 
            별도의 <a> 태그 없이 Link에 className을 직접 주는 방식을 권장합니다.
          */}
          <Link href={`/posts/${post.id}`} className="block hover:opacity-90">
            <img
              src={post.thumbNail}
              alt={post.title}
              className="w-full h-40 object-cover mb-2 rounded"
            />
            <h3 className="font-bold text-lg">{post.title}</h3>
            <p className="mt-1">상품명: {post.productName}</p>
            <p className="mt-1">가격: {post.productPrice}원</p>
            <p className="mt-1 text-sm text-gray-500">
              작성자: {post.writerName}
            </p>
            <p className="mt-1 text-xs text-gray-400">
              {new Date(post.createdAt).toLocaleDateString()}
            </p>
          </Link>
        </li>
      ))}
    </ul>
  );
}
