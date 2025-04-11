"use client";

import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import client from "@/lib/client";
import { components } from "@/lib/backend/apiV1/schema";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'; 
import { faLocationDot } from '@fortawesome/free-solid-svg-icons';
import { LocateFixedIcon, LocateIcon } from "lucide-react"

type LocationResponse = components["schemas"]["LocationResponse"];

interface LocationSectionProps {
  isLogin: boolean;
}

export default function LocationSection({ isLogin }: LocationSectionProps) {
  const [locationPosts, setLocationPosts] = useState<LocationResponse[]>([]);
  const [locationLoading, setLocationLoading] = useState<boolean>(false);
  const [locationError, setLocationError] = useState<string>("");
  const [currentPage, setCurrentPage] = useState<number>(1); // 현재 페이지 상태
  const [hasMore, setHasMore] = useState<boolean>(true); // 더 불러올 데이터가 있는지 여부
  const router = useRouter();
  const [radius, setRadius] = useState<number>(10); // 기본값 10km
  const [tempRadius, setTempRadius] = useState(10); 

  // 위치 기반 게시글 불러오기
  const fetchLocationPosts = async (page: number) => {
    setLocationLoading(true);
    setLocationError("");

    try {
      const response = await client.GET("/api/posts/location", {
        params: {
          query: {
            radius: radius,
            page: page, // 페이지 번호 추가
            pageSize: 5, // 한 번에 불러올 게시글 수
          },
        },
        credentials: "include", // 쿠키 포함
      });

      if (response.error) {
        throw new Error(response.error.message);
      }

      const newPosts = response.data.data.items || [];
      if (page === 1) {
        // 첫 페이지인 경우 기존 데이터를 초기화
        setLocationPosts(newPosts);
      } else {
        // 추가 페이지인 경우 기존 데이터에 병합
        setLocationPosts((prevPosts) => [...prevPosts, ...newPosts]);
      }

      // 더 불러올 데이터가 있는지 확인
      setHasMore(newPosts.length === 5); // 페이지 크기와 일치하면 더 있는 것으로 간주
    } catch (err) {
      console.error("❌ 위치 기반 게시글 불러오기 실패:", err);
      setLocationError("위치 기반 게시글을 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLocationLoading(false);
    }
  };

  // 초기 데이터 불러오기
  useEffect(() => {
    if (isLogin) {
      fetchLocationPosts(1); // 첫 페이지 데이터 불러오기
    }
  }, [radius, isLogin]);

  // 더보기 버튼 클릭 시 다음 페이지 데이터 불러오기
  const handleLoadMore = () => {
    const nextPage = currentPage + 1;
    setCurrentPage(nextPage);
    fetchLocationPosts(nextPage);
  };

  const handleSliderChange = (e:any) => {
    setTempRadius(Number(e.target.value)); // 슬라이더 값 변경 시 임시 값 업데이트
  };

  const handleApplyClick = () => {
    setRadius(tempRadius); // 버튼 클릭 시 실제 반경 값 업데이트
  };

  return (
    <section className="rounded-xl shadow-md p-6 bg-white mb-6">
      <div className="flex justify-between items-center mb-4">
      <div className="flex items-center">
        <Button>
          <FontAwesomeIcon icon={faLocationDot} className="mr-2 text-red-500" /> {/* 아이콘 추가 */}
          <h1 className="text-lg font-bold">가까운 상품</h1>
        </Button>
      </div>

      <div className="flex items-center gap-4">
        {/* 슬라이더와 현재 값 표시 */}
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500">1km</span>
          <div className="relative w-64 pt-6 pb-8">
            <input
              type="range"
              value={tempRadius}
              onChange={handleSliderChange}
              className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer z-10"
              min="1"
              max="40"
              step="1"
            />
            <div className="absolute w-full flex justify-between text-xs px-2" style={{ top: '0' }}>
              {[0, 10, 20, 30, 40].map((value) => (
                <div key={value} className="flex flex-col items-center">
                  <span>{value}</span>
                  <div className="h-2 w-0.5 bg-gray-300 mt-1"></div>
                </div>
              ))}
            </div>
          </div>
          <span className="text-sm text-gray-500">40km</span>
        </div>

        {/* 반경 값 표시 */}
        <span className="text-sm font-semibold">반경 {tempRadius}km</span>

        {/* 적용 버튼 */}
        <Button variant="outline" onClick={handleApplyClick}>
          확인
        </Button>
      </div>     
      </div>
      {locationLoading ? (
        <p className="text-gray-500">위치 기반 게시글을 불러오는 중...</p>
      ) : locationError ? (
        <p className="text-red-500">{locationError}</p>
      ) : locationPosts.length === 0 ? (
        <p className="text-gray-500">해당 반경 내에 게시글이 없습니다.</p>
      ) : (
        <>
          <div className="grid grid-cols-5 gap-4">
            {locationPosts.map((post) => (
              <div
                key={post.id}
                onClick={() => router.push(`/posts/${post.id}`)}
                className="p-4 border border-gray-200 rounded-md hover:bg-gray-50 cursor-pointer"
              >
                <img
                  src={post.thumbNail}
                  alt={post.title}
                  className="w-full h-32 object-cover rounded-md mb-2"
                />
                <h3 className="font-semibold">{post.title}</h3>
                <p className="text-sm text-gray-500">{post.productName}</p>
                <p className="text-sm text-gray-500">{post.distance}</p>
                <div className="text-right">
                  <p className="font-semibold">
                    {post.productPrice.toLocaleString()}원
                  </p>
                  <p className="text-sm text-gray-500">
                    {new Date(post.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
            ))}
          </div>
          {/* 더보기 버튼 */}
          {hasMore && (
            <div className="flex justify-center mt-4">
              <Button
                onClick={handleLoadMore}
                className="px-6 py-2"
                disabled={locationLoading}
              >
                {locationLoading ? "불러오는 중..." : "더보기"}
              </Button>
            </div>
          )}
        </>
      )}
    </section>
  );
}