"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import PageSizeSelector from "../_components/common/PageSizeSelector";
import Pagination from "../_components/common/Pagination";
import AdminListSuperAdminAccordian from "../_components/adminlist/AdminListSuperAdminAccordian";
import { AdminListSuperAdminItem } from "@/app/_type/AdminListSuperAdminItem";
import client from "@/lib/client";
import { Button } from "@/components/ui/button";
import AdminSignUp from "./AdminSignUp";

export default function AdminListSuperAdmin() {
  const router = useRouter();
  const [page, setPage] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);
  const [adminList, setAdminList] = useState<AdminListSuperAdminItem[]>([]);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [showAdminSignUp, setShowAdminSignUp] = useState(false);

  const getAdmins = async () => {
    const result = await client.GET("/api/admin/admins", {
      params: {
        query: {
          size: pageSize,
          page: page,
          pageable: {},
        },
      },
      credentials: "include",
    });

    if (result.error) {
      console.error(result.response);
      return;
    }

    setAdminList(result.data!.data.content!);
    setTotalPages(result.data!.data.totalPages!);
  };

  useEffect(() => {
    getAdmins();
  }, [page, pageSize]);

  // 신규 관리자 등록 버튼 핸들러 (모달 오픈)
  const handleNewAdmin = () => {
    setShowAdminSignUp(true);
  };

  return (
    <div className="relative w-full flex-1 flex flex-col min-h-0">
      <div className="flex-1 flex flex-col min-h-0">
        <div className="flex justify-end items-end">
          <PageSizeSelector
            selectedPageSize={pageSize}
            setSelectedPageSize={setPageSize}
          />
        </div>
        <AdminListSuperAdminAccordian items={adminList} />
        <Pagination
          currentPage={page}
          onPageChange={(newPage) => setPage(newPage)}
          totalPages={totalPages}
        />
      </div>
    </div>
  );
}
