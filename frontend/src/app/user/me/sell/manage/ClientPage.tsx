"use client";

import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";
import { useRouter, useSearchParams } from "next/navigation";
import { use, useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import Link from "next/link";
import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";

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
  const [status, setStatus] = useState<string | undefined>(
    searchParams.get("status") || undefined
  );
  const [pageSize, setPageSize] = useState<number>(
    Number(searchParams.get("pageSize")) || 10
  );
  const [sort, setSort] = useState<string>(searchParams.get("sort") || "desc");

  const handleSortChange = (newSort: string) => {
    setSort(newSort);
    const params = new URLSearchParams(searchParams.toString());

    params.set("sort", newSort);
    router.push(`/user/me/sell/manage?${params.toString()}`);
  };

  const handleFilterChange = (newStatus?: string) => {
    setStatus(newStatus);
    const params = new URLSearchParams(searchParams.toString());
    if (newStatus) {
      params.set("status", newStatus);
    } else {
      params.delete("status");
    }

    router.push(`/user/me/sell/manage?${params.toString()}`);
  };

  const handlePageChange = (page: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("page", page.toString());

    router.push(`/user/me/sell/manage?${params.toString()}`);
  };

  // 페이지 크기 변경 핸들러
  const handlePageSizeChange = (newSize: number) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set("pageSize", newSize.toString());
    router.push(`/user/me/sell/manage?${params.toString()}`);
  };

  const handleStatusChange = (postId?: string, newStatus?: string) => {
    // TODO: 상태 변경
  };

  return (
    <div className="space-y-1">
      <h2>내 상품 관리</h2>

      {/* 필터 */}
      <div className="flex flex-col mb-4">
        {/* 전체, 판매중, 예약중, 판매완료 */}
        <div className="flex space-x-2">
          <div>
            {[
              { label: "전체", value: undefined },
              { label: "판매중", value: "AVAILABLE" },
              { label: "예약중", value: "RESERVED" },
              { label: "판매완료", value: "PURCHASED" },
            ].map(({ label, value }) => (
              <Button
                key={value || ""}
                className={`px-3 py-1 border ${
                  status === value
                    ? "bg-gray-800 text-white"
                    : "bg-white text-black"
                }`}
                onClick={() => handleFilterChange(value)}
              >
                {label}
              </Button>
            ))}
          </div>
          {/* 최신순, 오래된순 */}
          <div className="flex ml-auto">
            <Select onValueChange={handleSortChange} defaultValue={sort}>
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder={sort} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="desc">최신순</SelectItem>
                <SelectItem value="asc">오래된순</SelectItem>
              </SelectContent>
            </Select>
          </div>

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

      {/* 상품 목록 */}
      {/* 썸네일, 상품명, 가격, 찜, 판매상태, 최근 수정일, 수정 */}
      <div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>사진</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>가격</TableHead>
              <TableHead>찜</TableHead>
              <TableHead>판매 상태</TableHead>
              <TableHead>작성 날짜</TableHead>
              <TableHead>수정하기</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {postInfo.length > 0 ? (
              postInfo.map((post) => (
                <TableRow key={post.id}>
                  {/* 상품 이미지 */}
                  <TableCell>
                    <Link href={`/posts/${post.id}`}>
                      <img src={post.thumbNail} alt="사진 없음" />
                    </Link>
                  </TableCell>

                  {/* 상품명 */}
                  <TableCell>
                    <Link href={`/posts/${post.id}`}>{post.title}</Link>
                  </TableCell>

                  {/* 가격 */}
                  <TableCell>{post.productPrice?.toLocaleString()}원</TableCell>

                  {/* 찜 */}
                  <TableCell>{post.likedCount}</TableCell>

                  {/* 판매 상태 */}
                  <TableCell>
                    {/* 향후에 상품 글 상태 변경하게  */}
                    <Select
                      onValueChange={(value) =>
                        handleStatusChange(post.id, value)
                      }
                      defaultValue={post.status}
                    >
                      <SelectTrigger className="w-[150px]">
                        <SelectValue>
                          {post.status === "AVAILABLE"
                            ? "판매중"
                            : post.status === "RESERVED"
                              ? "예약중"
                              : "판매 완료"}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="AVALIABLE">판매중</SelectItem>
                        <SelectItem value="RESERVED">예약중</SelectItem>
                        <SelectItem value="PURCHASED">판매완료</SelectItem>
                      </SelectContent>
                    </Select>
                  </TableCell>

                  {/* 최근 수정일 */}
                  <TableCell>
                    {post.createdAt ? new Date().toLocaleDateString() : "-"}
                  </TableCell>

                  {/* 수정하기 */}
                  <TableCell>
                    <Link href={`/post/edit/${post.id}`}>
                      <Button>수정</Button>
                    </Link>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell>등록된 상품이 없습니다.</TableCell>
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
