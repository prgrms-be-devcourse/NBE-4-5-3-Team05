"use client";

import { components } from "@/lib/backend/apiV1/schema";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@radix-ui/react-select";
import Link from "next/link";
import { Button } from "@/components/ui/button";

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
  const router = useRouter();
  const searchParams = useSearchParams();
  const [pageSize, setPageSize] = useState<number>(
    Number(searchParams.get("pageSize")) || 10
  );

  const handlePageChange = (page: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("page", page.toString());

    router.push(`/user/me/purchase/history?${params.toString()}`);
  };

  // 페이지 크기 변경 핸들러
  const handlePageSizeChange = (newSize: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("pageSize", newSize.toString());
    router.push(`/user/me/purchase/history?${params.toString()}`);
  };

  return (
    <div className="space-y-1">
      <h2>구매 내역</h2>

      {/* 필터 */}
      <div className="flex flex-col mb-4">
        {/* 전체, 판매중, 예약중, 판매완료 */}
        <div className="flex space-x-2">
          {/* 페이지 크기 선택 */}
          <Select
            onValueChange={(value) => handlePageSizeChange(Number(value))}
            defaultValue={pageSize.toString()}
          >
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder={pageSize} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="5">5개씩</SelectItem>
              <SelectItem value="10">10개씩</SelectItem>
              <SelectItem value="20">20개씩</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* 구매 목록 */}
      {/* 썸네일, 상품명, 가격 */}
      <div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>사진</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>가격</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {postInfo.length > 0 ? (
              postInfo.map((post) => (
                <TableRow key={post.id}>
                  {/* 상품 이미지 */}
                  <TableCell>
                    <Link href={`/post/${post.id}`}>
                      <img src={post.thumbNail} alt="사진 없음" />
                    </Link>
                  </TableCell>
                  {/* 상품명 */}
                  <TableCell>
                    <Link href={`/post/${post.id}`}>{post.title}</Link>
                  </TableCell>
                  {/* 가격 */}
                  <TableCell>{post.productPrice?.toLocaleString()}원</TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell>구매한 상품이 없습니다.</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* 페이징 번호 */}
      <div>
        {Array.from({ length: pageInfo.totalPages }, (_, i) => (
          <Button
            key={i}
            className={`border ${pageInfo.currentPage === i + 1 ? "bg-gray-800 text-white" : "bg-white text-black"}`}
            onClick={() => handlePageChange(i + 1)}
          >
            {i + 1}
          </Button>
        ))}
      </div>
    </div>
  );
}
