// components/Notice.tsx
//공지사항 컴포넌트
"use client";

import React from "react";

interface NoticeProps {
  title: string;
  content: string;
}

export default function Notice({ title, content }: NoticeProps) {
  return (
    <section className="bg-red-100 p-4 mb-4 rounded shadow">
      <h2 className="text-xl font-bold mb-2">{title}</h2>
      <p>{content}</p>
    </section>
  );
}
