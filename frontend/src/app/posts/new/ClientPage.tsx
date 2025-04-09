"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import type { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import fileUploadClient from "@/lib/fileUploadClient";
import MapPopup from "@/components/MapPopup"; // MapPopup 임포트

type Category = components["schemas"]["Category"];

export default function PostCreatePage() {
  const router = useRouter();

  // 입력값 state
  const [productName, setProductName] = useState("");
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [price, setPrice] = useState<number | "">("");
  // 거래 위치(주소)는 검색 후 업데이트되며, 수동 수정도 가능
  const [location, setLocation] = useState("");
  // 지도 선택 시 업데이트되는 위도, 경도
  const [latitude, setLatitude] = useState<number | "">("");
  const [longitude, setLongitude] = useState<number | "">("");
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  // 지도 팝업 열림 상태 관리
  const [isMapOpen, setIsMapOpen] = useState(false);
  // MapPopup에 전달할 현재 지도 위치 (초기값은 서울 대신 사용자의 위치로 업데이트)
  const [mapCurrentPos, setMapCurrentPos] = useState({
    lat: 37.5665,
    lng: 126.978,
    zoom: 12,
  });

  // 컴포넌트가 마운트될 때 사용자의 위치를 가져와 디폴트 위치로 설정
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setMapCurrentPos({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
            zoom: 12,
          });
        },
        (error) => {
          console.error("위치 정보를 가져오는데 실패했습니다.", error);
        }
      );
    } else {
      console.error("Geolocation API를 사용할 수 없습니다.");
    }
  }, []);

  // AWS S3 업로드 API 호출 함수
  const uploadFile = async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await fileUploadClient.POST("/api/uploadFile", {
      body: formData as any,
      rawBody: true,
      credentials: "include",
      headers: {},
    });
    if (response.error) {
      console.error("파일 업로드 실패", response.error);
    }
    return response.data as string;
  };

  useEffect(() => {
    async function fetchCategories() {
      const res = await client.GET("/api/categories", {
        credentials: "include",
      });
      if (res.error) {
        console.error("카테고리 불러오기 실패", res.error);
        return;
      }
      setCategories(res.data!.data);
    }
    fetchCategories();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!productName) {
      alert("물품 이름을 입력해주세요.");
      return;
    }
    if (!selectedCategory) {
      alert("카테고리를 선택해주세요.");
      return;
    }
    if (!title.trim()) {
      alert("게시글 제목을 입력해주세요.");
      return;
    }
    if (!content.trim()) {
      alert("본문 내용을 입력해주세요.");
      return;
    }
    if (price === "" || Number(price) <= 0) {
      alert("유효한 가격을 입력해주세요.");
      return;
    }
    if (location.trim() === "") {
      alert("거래 위치를 입력해주세요.");
      return;
    }
    if (latitude === "" || isNaN(Number(latitude))) {
      alert("유효한 위도를 선택해주세요.");
      return;
    }
    if (longitude === "" || isNaN(Number(longitude))) {
      alert("유효한 경도를 선택해주세요.");
      return;
    }

    let uploadedUrls: string[] = [];
    if (selectedFiles && selectedFiles.length > 0) {
      uploadedUrls = await Promise.all(
        Array.from(selectedFiles).map((file) => uploadFile(file))
      );
    }
    const categoryIds = [Number(selectedCategory)];

    const data = {
      productName,
      productPrice: Number(price),
      title,
      content,
      categoryIds,
      imageUrlList: uploadedUrls,
      latitude: Number(latitude),
      longitude: Number(longitude),
    };

    const result = await client.POST("/api/posts", {
      body: data,
      credentials: "include",
    });
    if (result.error) {
      if (result.response.status === 401) {
        alert("로그인을 먼저하세요.");
        router.push("/user/login");
        return;
      }
      console.error("게시글 작성 실패", result.error);
      return;
    }

    router.push("/posts");
  };

  const openMap = () => {
    setIsMapOpen(true);
  };

  const closeMap = () => {
    setIsMapOpen(false);
  };

  // MapPopup에서 위치 선택 시 좌표와 주소 업데이트
  const handleLocationSelect = (
    lat: number,
    lng: number,
    zoom: number,
    address: string
  ) => {
    setLatitude(lat);
    setLongitude(lng);
    setLocation(address);
    setMapCurrentPos({ lat, lng, zoom });
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex space-x-4 p-4 w-full flex-1 relative"
    >
      {/* 왼쪽 영역 */}
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
            className="border w-full p-2 h-48 resize-none"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="상품 설명, 상태 등을 자세히 적어주세요."
          />
        </div>
      </div>

      {/* 오른쪽 영역 */}
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
            placeholder="주소를 선택하세요"
          />
          <button
            type="button"
            onClick={openMap}
            className="mt-2 w-full bg-gray-200 p-2 rounded"
          >
            지도에서 위치 선택
          </button>
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">위도</label>
          <input
            type="number"
            className="border w-full p-2"
            value={latitude}
            readOnly
          />
        </div>
        <div className="mb-4">
          <label className="block mb-1 font-semibold">경도</label>
          <input
            type="number"
            className="border w-full p-2"
            value={longitude}
            readOnly
          />
        </div>
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

      {/* 지도 팝업 모달 */}
      {isMapOpen && (
        <MapPopup
          currentPos={mapCurrentPos}
          onLocationSelect={handleLocationSelect}
          onClose={closeMap}
        />
      )}
    </form>
  );
}
