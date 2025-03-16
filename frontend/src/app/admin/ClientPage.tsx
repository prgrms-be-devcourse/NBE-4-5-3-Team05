"use client";

import { lazy, useEffect, useState } from "react";
import { Home, User, Settings } from "lucide-react";
import { useRouter } from "next/navigation";

const ProductListAdmin = lazy(() => import("./_pages/ProductListAdmin"));
const NoticeAdmin = lazy(() => import("./_pages/NoticeAdmin"));
const UserListAdmin = lazy(() => import("./_pages/UserListAdmin"));
const SystemLog = lazy(() => import("./_pages/SystemLog"));

type PageType = "ProductList" | "Notice" | "UserList" | "SystemLog";

interface SidebarProps {
  setPage: (page: PageType) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ setPage }) => {
  return (
    <div className="w-64 bg-gray-900 text-white p-5 flex flex-col gap-4 ">
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
    </div>
  );
};

interface ContentProps {
  page: PageType;
}

const Content: React.FC<ContentProps> = ({ page }) => {
  return (
    <div className="flex flex-col flex-1 p-5">
      {page === "ProductList" && <ProductListAdmin></ProductListAdmin>}
      {page === "UserList" && <UserListAdmin></UserListAdmin>}
      {page === "Notice" && <NoticeAdmin></NoticeAdmin>}
      {page === "SystemLog" && <SystemLog></SystemLog>}
    </div>
  );
};

export default function SidebarLayout({
  searchParams,
}: {
  searchParams: {
    tab: string;
  };
}) {
  const router = useRouter();
  const { tab } = searchParams;

  const [currentPage, setCurrentPage] = useState<PageType>(
    (tab as PageType) || ("ProductList" as PageType)
  );

  useEffect(() => {
    if (!tab) {
      router.replace("?tab=ProductList"); // ✅ 초기 상태 설정
    }
  }, [tab, router]);

  // URL에서 현재 탭 상태 가져오기

  // 페이지 변경 시 URL 업데이트
  const setPage = (page: PageType) => {
    router.push(`?tab=${page}`, { scroll: false });
  };
  return (
    <div className="flex flex-1 overflow-y-scroll ">
      <Sidebar setPage={setPage} />
      <Content page={currentPage} />
    </div>
  );
}
