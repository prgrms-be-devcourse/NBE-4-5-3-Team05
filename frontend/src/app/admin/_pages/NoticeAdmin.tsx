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
} from "@/components/ui/breadcrumb";

export default function NoticeAdmin() {
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);
  const [totalPages, setTotalPageSize] = useState<number>(0);
  const [notices, setNotices] = useState<NoticeListItem[]>([]);

  const [currentView, setCurrentView] = useState<"list" | "form">("list");
  const [editNotice, setEditNotice] = useState<NoticeListItem | null>(null);

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

  const deleteNotice = async (notice: NoticeListItem) => {
    const response = await client.DELETE("/api/admin/notices/{notice-id}", {
      params: {
        path: {
          "notice-id": notice.id!,
        },
      },
      credentials: "include",
    });
    if (response.error) {
      console.log(response.error);
      return;
    }
    alert("삭제 성공.");
    window.location.reload();
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
          <NoticeListAccordian
            items={notices}
            onEdit={(notice) => {
              setEditNotice(notice);
              setCurrentView("form");
            }}
            onDelete={(notice) => {
              const userConfirmed = window.confirm("정말로 삭제하시겠습니까?");
              if (userConfirmed) {
                deleteNotice(notice);
                // 여기에 실행할 동작을 추가하세요
              } else {
                console.log("사용자가 취소를 클릭했습니다.");
              }
            }}
          />
          <Pagination
            currentPage={page}
            totalPages={totalPages > 0 ? totalPages - 1 : 0}
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
          <NoticeForm editNotice={editNotice!} />
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
