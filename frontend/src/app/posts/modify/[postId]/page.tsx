"use client";

import React, { useContext, useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import type { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import fileUploadClient from "@/lib/fileUploadClient";
import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import MapComponent from "@/components/MapComponent";

type Category = components["schemas"]["Category"];
type ProductPostResponse = components["schemas"]["ProductPostResponse"];

export default function PostModifyPage() {
  const { postId } = useParams<{ postId: string }>();
  const router = useRouter();
  // LoginMemberContext에서 로그인 정보를 직접 가져옵니다.
  const { loginMember } = useContext(LoginMemberContext);

  // 수정할 필드 상태들
  const [productName, setProductName] = useState("");
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [price, setPrice] = useState<number | "">("");
  const [location, setLocation] = useState("");
  const [latitude, setLatitude] = useState<number | "">("");
  const [longitude, setLongitude] = useState<number | "">("");
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [initialImageUrls, setInitialImageUrls] = useState<string[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");

  // 로그인 정보 디버깅
  useEffect(() => {
    console.log("수정 페이지의 loginMember:", loginMember);
  }, [loginMember]);

  // AWS S3 파일 업로드 함수
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

  // 카테고리 목록 불러오기
  const fetchCategories = async () => {
    try {
      const res = await client.GET("/api/categories", {
        credentials: "include",
      });
      if (res.error) {
        console.error("카테고리 불러오기 실패", res.error);
        return;
      }
      setCategories(res.data.data);
    } catch (err) {
      console.error("카테고리 불러오는 중 오류 발생", err);
    }
  };

  // 기존 게시글 데이터 불러오기 및 상태 초기화
  const fetchPost = async () => {
    if (!postId) return;
    try {
      const response = await client.GET("/api/posts/{id}", {
        withCredentials: true,
        params: { path: { id: postId } },
      });
      if (response.error) {
        setError("게시글 정보를 불러올 수 없습니다.");
      } else {
        const postData = response.data.data;
        setProductName(postData.productName ?? "");
        setTitle(postData.title ?? "");
        setContent(postData.content ?? "");
        if (postData.categories && postData.categories.length > 0) {
          // 카테고리가 문자열 배열로 전달된다고 가정
          setSelectedCategory(postData.categories[0]);
        }
        setPrice(postData.productPrice ?? "");
        setLatitude(postData.latitude ?? "");
        setLongitude(postData.longitude ?? "");
        setLocation("");
        if (postData.imageUrls) {
          const imgs = postData.imageUrls
            .split(",")
            .map((url) => url.trim())
            .filter((url) => url && url.toLowerCase() !== "null");
          setInitialImageUrls(imgs);
        }
      }
    } catch (err) {
      console.error("게시글 데이터를 불러오는 중 오류 발생", err);
      setError("게시글 정보를 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 게시글 수정 처리
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // 파일 업로드 처리
    let uploadedUrls: string[] = [];
    if (selectedFiles && selectedFiles.length > 0) {
      uploadedUrls = await Promise.all(
        Array.from(selectedFiles).map((file) => uploadFile(file))
      );
    }
    const imageUrlList =
      uploadedUrls.length > 0 ? uploadedUrls : initialImageUrls;
    const categoryIds = [Number(selectedCategory)];

    const data = {
      productName,
      productPrice: Number(price),
      title,
      content,
      categoryIds,
      imageUrlList,
      latitude: Number(latitude),
      longitude: Number(longitude),
      location,
    };

    // 수정 요청 전, 로그인 정보가 있는지 확인
    if (!loginMember.id) {
      alert("로그인이 필요합니다.");
      router.push("/user/login");
      return;
    }

    try {
      const response = await client.PUT("/api/posts/{id}", {
        credentials: "include",
        params: { path: { id: postId } },
        body: data,
      });
      if (response.error) {
        alert("수정에 실패했습니다: " + response.error.message);
        return;
      }
      alert("수정이 완료되었습니다.");
      router.push(`/posts/${postId}`);
    } catch (err) {
      alert("수정 중 오류가 발생했습니다.");
      console.error(err);
    }
  };

  useEffect(() => {
    fetchPost();
    fetchCategories();
  }, [postId]);

  if (loading) return <div className="p-4">로딩 중...</div>;
  if (error) return <div className="p-4 text-red-500">{error}</div>;

  return (
    <div className="p-4 w-full">
      <h1 className="text-2xl font-bold mb-4">판매글 수정</h1>
      <form
        onSubmit={handleSubmit}
        className="flex space-x-4 p-4 w-full flex-1"
      >
        {/* 왼쪽 영역: 물품 이름, 게시글 제목, 본문 */}
        <div className="w-2/3 space-y-4">
          <div>
            <label className="block mb-1 font-semibold">물품 이름</label>
            <input
              type="text"
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
              className="border p-2 w-full"
              placeholder="예) 아이폰 14 프로"
            />
          </div>
          <div>
            <label className="block mb-1 font-semibold">게시글 제목</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="border p-2 w-full"
              placeholder="예) 아이폰 14 프로 팝니다"
            />
          </div>
          <div>
            <label className="block mb-1 font-semibold">본문</label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              className="border p-2 w-full h-40"
              placeholder="상품 설명, 상태 등을 자세히 적어주세요."
            />
          </div>
        </div>

        {/* 오른쪽 영역: 카테고리, 가격, 거래 위치, 좌표, 파일 업로드 */}
        <div className="w-1/3 space-y-4">
          <div>
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
          <div>
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
          <div>
            <label className="block mb-1 font-semibold">거래 위치</label>
            <input
              type="text"
              className="border w-full p-2"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
            />
          </div>
          <div>
            <label className="block mb-1 font-semibold">위도</label>

            <MapComponent
              onLocationSelect={(lat, lng) => {
                console.log(lat, lng);
              }}
            ></MapComponent>
            <input
              type="number"
              className="border w-full p-2"
              value={latitude}
              onChange={(e) =>
                setLatitude(e.target.value === "" ? "" : Number(e.target.value))
              }
            />
          </div>
          <div>
            <label className="block mb-1 font-semibold">경도</label>
            <input
              type="number"
              className="border w-full p-2"
              value={longitude}
              onChange={(e) =>
                setLongitude(
                  e.target.value === "" ? "" : Number(e.target.value)
                )
              }
            />
          </div>
          <div>
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
            수정 완료
          </button>
        </div>
      </form>
    </div>
  );
}
