import { useEffect, useState } from "react";
import NoticeForm from "./NoticeForm";
import NoticeListAccordian from "./NoticeListAccordian";
import client from "@/lib/client";
import { NoticeListItem } from "@/app/_type/NoticeListItem";
import PageSizeSelector from "../_components/common/PageSizeSelector";
import Pagination from "../_components/common/Pagination";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";

export default function NoticeAdmin() {
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);
  const [totalPages, setTotalPageSize] = useState<number>(0);
  const [notices, setNotices] = useState<NoticeListItem[]>([]);

  const [currentView, setCurrentView] = useState<"list" | "form">("list");

  const getNotices = async () => {
    const response = await client.GET("/api/admin/notices", {
      params: {
        query: {
          pageable: {},
          size: size,
          page: page,
        },
      },
      credentials: "include",
    });
    if (response.error) {
      console.log(response.error);
      return;
    }
    console.log(response.data!.data.content!);
    setNotices(response.data!.data.content!);
    setTotalPageSize(response.data!.data.totalPages!);
  };
  useEffect(() => {
    if (currentView == "list") {
      getNotices();
    }
  }, [page, totalPages, size, currentView]);
  return (
    <div className="h-full flex flex-col">
      {/* Breadcrumbs 네비게이션 */}
      <Breadcrumb className="mb-4 gap-2 flex">
        <BreadcrumbItem>
          <BreadcrumbLink
            className="font-semibold"
            href=""
            onClick={() => setCurrentView("list")}
          >
            Notice
          </BreadcrumbLink>
        </BreadcrumbItem>
        <p>{">"}</p>
        <BreadcrumbItem>
          <BreadcrumbLink href="#" className="font-semibold">
            {currentView === "list" ? "" : "Form"}
          </BreadcrumbLink>
        </BreadcrumbItem>
      </Breadcrumb>

      {/* 공지 목록 & 공지 작성 화면 전환 */}
      {currentView === "list" ? (
        <div className="h-full flex flex-col flex-1">
          <PageSizeSelector
            selectedPageSize={size}
            setSelectedPageSize={setSize}
          />
          <NoticeListAccordian items={notices} />
          <Pagination
            currentPage={page}
            totalPages={totalPages - 1}
            onPageChange={setPage}
          />
          <button
            onClick={() => setCurrentView("form")}
            className="mt-4 p-2 bg-blue-500 text-white rounded"
          >
            공지 작성하기
          </button>
        </div>
      ) : (
        <div className="h-full flex flex-col flex-1">
          <NoticeForm />
          <button
            onClick={() => setCurrentView("list")}
            className="mt-4 p-2 bg-gray-500 text-white rounded"
          >
            목록으로 돌아가기
          </button>
        </div>
      )}
    </div>
  );
}
