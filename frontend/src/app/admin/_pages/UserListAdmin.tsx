"use client";

import PageSizeSelector from "../_components/common/PageSizeSelector";
import Pagination from "../_components/common/Pagination";
import UserListAccordian from "../_components/userlist/UserListAccordian";
import { useEffect, useState } from "react";
import { UserListItem } from "@/app/_type/UserListItem";
import client from "@/lib/client";

export default function UserListAdmin() {
  const [page, setPage] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);
  const [userlist, setUserList] = useState<UserListItem[]>([]);
  const [totalPages, setTotalPages] = useState<number>(0);

  const getUsers = async () => {
    const result = await client.GET("/api/admin/users", {
      params: {
        query: {
          size: pageSize,
          page: page,
          pageable: {},
        },
      },
    });
    if (result.error) {
      console.log(result.response);
      return;
    }
    setUserList(result.data!.data.content!);
  };

  useEffect(() => {
    getUsers();
  }, [page, pageSize, totalPages]);
  return (
    <div className="w-full flex-1 flex flex-col min-h-0">
      <div className="flex-1 flex flex-col min-h-0">
        <h1>UserList</h1>
        <div className="flex justify-end items-end">
          <PageSizeSelector
            selectedPageSize={pageSize}
            setSelectedPageSize={setPageSize}
          ></PageSizeSelector>
        </div>

        <UserListAccordian items={userlist}></UserListAccordian>
        <Pagination
          currentPage={page}
          onPageChange={(page) => {
            setPage(page);
          }}
          totalPages={totalPages}
        ></Pagination>
      </div>
    </div>
  );
}
