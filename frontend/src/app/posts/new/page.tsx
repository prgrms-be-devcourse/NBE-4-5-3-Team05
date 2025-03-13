"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import type { components } from "@/lib/backend/apiV1/schema";

// OpenAPI 스키마의 Category 타입 사용 (선택적 속성일 수 있으므로 필요한 경우 non-null assertion이나 default 값을 설정)
type Category = components["schemas"]["Category"];

export default function PostCreatePage() {
  const router = useRouter();

  // 로그인 체크
  useEffect(() => {
    async function checkAuth() {
      try {
        await axios.get("/api/users/me", { withCredentials: true });
      } catch (err: any) {
        if (err.response && err.response.status === 401) {
          alert("로그인을 먼저하세요.");
          router.push("/user/login");
        }
      }
    }
    checkAuth();
  }, [router]);

  // 입력값 state
  const [productName, setProductName] = useState("");
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  // 선택한 카테고리 id (문자열로 받아서 후에 number로 변환)
  const [selectedCategory, setSelectedCategory] = useState("");
  const [price, setPrice] = useState<number | "">("");
  const [location, setLocation] = useState("");
  const [latitude, setLatitude] = useState<number | "">("");
  const [longitude, setLongitude] = useState<number | "">("");
  // 이미지 파일 선택 state
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  // 업로드된 이미지 URL 리스트
  const [imageUrlList, setImageUrlList] = useState<string[]>([]);
  // 백엔드에서 불러온 카테고리 목록 state (OpenAPI 스키마의 Category 타입 사용)
  const [categories, setCategories] = useState<Category[]>([]);

  // AWS S3 업로드 API 호출 함수
  const uploadFile = async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await axios.post("/api/uploadFile", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      withCredentials: true,
    });
    return response.data;
  };

  // 백엔드에서 카테고리 목록 불러오기 (스키마 기반 타입 사용)
  useEffect(() => {
    async function fetchCategories() {
      try {
        // schema.d.ts에 정의된 Category 배열을 반환하는 엔드포인트 호출
        const res = await axios.get<{
          code: string;
          message: string;
          data: Category[];
        }>("/api/categories", {
          withCredentials: true,
        });
        // 필요시 undefined 속성을 보완할 수 있음
        setCategories(res.data.data);
      } catch (err) {
        console.error("카테고리 로드 실패", err);
      }
    }
    fetchCategories();
  }, []);

  // 게시글 생성 처리
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      let uploadedUrls: string[] = [];
      if (selectedFiles && selectedFiles.length > 0) {
        uploadedUrls = await Promise.all(
          Array.from(selectedFiles).map((file) => uploadFile(file))
        );
      }
      setImageUrlList(uploadedUrls);

      // 선택한 카테고리 id를 숫자로 변환하여 배열에 넣음
      const categoryIds = selectedCategory ? [Number(selectedCategory)] : [];

      // 게시글 생성 API로 보낼 JSON 데이터 구성
      const data = {
        productName,
        productPrice: price === "" ? 0 : Number(price),
        title,
        content,
        categoryIds: categoryIds,
        imageUrlList: uploadedUrls,
        latitude: latitude === "" ? 0 : Number(latitude),
        longitude: longitude === "" ? 0 : Number(longitude),
      };

      await axios.post("/api/posts", data, {
        headers: { "Content-Type": "application/json" },
        withCredentials: true,
      });

      router.push("/posts");
    } catch (err: any) {
      console.error("게시글 작성 실패", err);
      if (err.response && err.response.status === 401) {
        alert("로그인을 먼저하세요.");
        router.push("/user/login");
      } else {
        alert("게시글 작성 중 오류가 발생했습니다.");
      }
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex space-x-4 p-4">
      {/* 왼쪽 영역: 물품 이름, 게시글 제목, 본문 */}
      <div className="w-2/3">
        <h1 className="text-2xl font-bold mb-4">판매 게시글 작성</h1>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">물품 이름</label>
          <input
            type="text"
            className="border w-full p-2"
            value={productName}
            onChange={(e) => setProductName(e.target.value)}
            placeholder="예) 아이폰 14 프로"
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">게시글 제목</label>
          <input
            type="text"
            className="border w-full p-2"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="예) 아이폰 14 프로 팝니다"
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">본문</label>
          <textarea
            className="border w-full p-2 h-48"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="상품 설명, 상태 등을 자세히 적어주세요."
          />
        </div>
      </div>

      {/* 오른쪽 영역: 카테고리, 가격, 거래 위치, 좌표, 사진 추가, 게시하기 버튼 */}
      <div className="w-1/3">
        <div className="mb-4">
          <label className="block mb-1 font-semibold">카테고리</label>
          <select
            className="border w-full p-2"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            <option value="">카테고리 선택</option>
            {categories.map((cat) => (
              <option key={cat.id!} value={cat.id!}>
                {cat.name}
              </option>
            ))}
          </select>
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">가격</label>
          <input
            type="number"
            className="border w-full p-2"
            value={price}
            onChange={(e) =>
              setPrice(e.target.value === "" ? "" : Number(e.target.value))
            }
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">거래 위치</label>
          <input
            type="text"
            className="border w-full p-2"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">위도</label>
          <input
            type="number"
            className="border w-full p-2"
            value={latitude}
            onChange={(e) =>
              setLatitude(e.target.value === "" ? "" : Number(e.target.value))
            }
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">경도</label>
          <input
            type="number"
            className="border w-full p-2"
            value={longitude}
            onChange={(e) =>
              setLongitude(e.target.value === "" ? "" : Number(e.target.value))
            }
          />
        </div>

        {/* 파일 입력 부분: 버튼으로 파일 선택 및 선택 파일 목록 표시 */}
        <div className="mb-4">
          <label className="block mb-1 font-semibold">사진 추가</label>
          <input
            id="file-upload"
            type="file"
            multiple
            className="hidden"
            onChange={(e) => setSelectedFiles(e.target.files)}
          />
          <label
            htmlFor="file-upload"
            className="cursor-pointer inline-block px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded"
          >
            파일 선택
          </label>
          {selectedFiles && selectedFiles.length > 0 && (
            <div className="mt-2">
              <p className="text-sm font-medium">선택한 파일:</p>
              <ul className="list-disc list-inside text-sm">
                {Array.from(selectedFiles).map((file, index) => (
                  <li key={index}>{file.name}</li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <button
          type="submit"
          className="mt-4 w-full bg-blue-500 text-white p-2 rounded"
        >
          게시하기
        </button>
      </div>
    </form>
  );
}
