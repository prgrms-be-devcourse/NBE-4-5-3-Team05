"use client";

import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import { useEffect, useState } from "react";
import ProductListAccordian from "../_components/productlist/ProductListAccordian";
import PageSizeSelector from "../_components/common/PageSizeSelector";
import Pagination from "../_components/common/Pagination";
import { ProductListItem } from "@/app/_type/ProductListItem";

export default function ProductListAdmin() {
  const [page, setPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(10);
  const [keyword, setKeyword] = useState<string>("");
  const [sort, setSort] = useState<string>("desc");
  const [productList, setProductList] = useState<ProductListItem[]>([]);
  const [totalPages, setTotalPages] = useState<number>(0);
  useEffect(() => {
    getProductList();
  }, [page, pageSize, keyword, sort]);
  const getProductList = async () => {
    const result = await client.GET("/api/posts", {
      params: {
        query: {
          keyword,
          page,
          pageSize,
          sort,
        },
      },
    });
    if (result.error) {
      console.log(result.response);
    }
    setProductList(result.data!.data.items);
    setTotalPages(result.data!.data.totalPages);
  };

  return (
    <div className="w-full flex flex-col flex-1 min-h-0">
      <div className="flex-1 flex flex-col min-h-0 ">
        <div className="flex justify-end items-end min-h-0">
          <PageSizeSelector
            selectedPageSize={pageSize}
            setSelectedPageSize={setPageSize}
          ></PageSizeSelector>
        </div>

        <ProductListAccordian items={productList}></ProductListAccordian>
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
