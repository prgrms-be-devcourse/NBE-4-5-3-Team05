"use client";

import { lazy, useEffect, useState, useContext } from "react";
import { Home, User, Settings, Shield } from "lucide-react";
import { useRouter } from "next/navigation";
import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";

// 기존 관리자 페이지들
const ProductListAdmin = lazy(() => import("./_pages/ProductListAdmin"));
const NoticeAdmin = lazy(() => import("./_pages/NoticeAdmin"));
const UserListAdmin = lazy(() => import("./_pages/UserListAdmin"));
const SystemLog = lazy(() => import("./_pages/SystemLog"));
const AdminListSuperAdmin = lazy(() => import("./_pages/AdminListSuperAdmin"));
const AdminSignUp = lazy(() => import("./_pages/AdminSignUp"));

type PageType =
  | "ProductList"
  | "Notice"
  | "UserList"
  | "SystemLog"
  | "AdminListSuperAdmin"
  | "AdminSignUp";

interface SidebarProps {
  setPage: (page: PageType) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ setPage }) => {
  // 로그인 정보 가져오기
  const { isSuperAdmin } = useContext(LoginMemberContext);

  return (
    <div className="w-64 bg-gray-900 text-white p-5 flex flex-col gap-4">
      <button
        onClick={() => setPage("ProductList")}
        className="flex items-center gap-2 p-3 hover:bg-gray-700 rounded"
      >
        <Home size={20} /> 상품 리스트
      </button>
      <button
        onClick={() => setPage("UserList")}
        className="flex items-center gap-2 p-3 hover:bg-gray-700 rounded"
      >
        <User size={20} /> 사용자 리스트
      </button>
      <button
        onClick={() => setPage("Notice")}
        className="flex items-center gap-2 p-3 hover:bg-gray-700 rounded"
      >
        <Settings size={20} /> 공지사항
      </button>
      <button
        onClick={() => setPage("SystemLog")}
        className="flex items-center gap-2 p-3 hover:bg-gray-700 rounded"
      >
        <Settings size={20} /> 로그
      </button>
      {isSuperAdmin && (
        <>
          <button
            onClick={() => setPage("AdminListSuperAdmin")}
            className="flex items-center gap-2 p-3 hover:bg-gray-700 rounded"
          >
            <Shield size={20} /> 관리자 리스트
          </button>
          <button
            onClick={() => setPage("AdminSignUp")}
            className="flex items-center gap-2 p-3 hover:bg-gray-700 rounded"
          >
            <Shield size={20} /> 관리자 등록
          </button>
        </>
      )}
    </div>
  );
};

interface ContentProps {
  page: PageType;
}

const Content: React.FC<ContentProps> = ({ page }) => {
  return (
    <div className="flex flex-col flex-1 p-5">
      {page === "ProductList" && <ProductListAdmin />}
      {page === "UserList" && <UserListAdmin />}
      {page === "Notice" && <NoticeAdmin />}
      {page === "SystemLog" && <SystemLog />}
      {page === "AdminListSuperAdmin" && <AdminListSuperAdmin />}
      {page === "AdminSignUp" && <AdminSignUp />}
    </div>
  );
};

export default function SidebarLayout({
  searchParams,
}: {
  searchParams: { tab: string };
}) {
  const router = useRouter();
  const { tab } = searchParams;
  const [currentPage, setCurrentPage] = useState<PageType>(
    (tab as PageType) || "ProductList"
  );

  useEffect(() => {
    if (!tab) {
      router.replace("?tab=ProductList"); // 초기 상태 설정
    }
    setCurrentPage(tab as PageType);
  }, [tab, router]);

  // 페이지 변경 시 URL 업데이트
  const setPage = (page: PageType) => {
    router.push(`?tab=${page}`, { scroll: false });
  };

  return (
    <div className="flex flex-1 w-full">
      <Sidebar setPage={setPage} />
      <Content page={currentPage} />
    </div>
  );
}
