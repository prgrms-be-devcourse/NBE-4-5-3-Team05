"use client";

import React, { useState } from "react";
import MapWithMarkers from "./MapComponent";

interface SearchResult {
  lat: string;
  lon: string;
  display_name: string;
}

interface MapPopupProps {
  currentPos: { lat: number; lng: number; zoom: number };
  onLocationSelect: (
    lat: number,
    lng: number,
    zoom: number,
    address: string
  ) => void;
  onClose: () => void;
}

export default function MapPopup({
  currentPos,
  onLocationSelect,
  onClose,
}: MapPopupProps) {
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  // 선택한 좌표, 줌, 그리고 주소를 state에 저장
  const [selectedPos, setSelectedPos] = useState<{
    lat: number;
    lng: number;
    zoom: number;
    address: string;
  } | null>(null);

  // 검색 API 호출 (OpenStreetMap Nominatim 사용)
  const handleSearch = async () => {
    if (!searchQuery) return;
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
          searchQuery
        )}`
      );
      const results: SearchResult[] = await response.json();
      if (results && results.length > 0) {
        setSearchResults(results);
      } else {
        alert("검색 결과가 없습니다.");
        setSearchResults([]);
      }
    } catch (error) {
      console.error("주소 검색 중 오류 발생", error);
      alert("검색 중 오류가 발생했습니다.");
    }
  };

  // 검색 결과 항목 클릭 시, state에 좌표와 address 저장(줌은 16으로 지정)
  const handleResultClick = (result: SearchResult) => {
    const lat = parseFloat(result.lat);
    const lng = parseFloat(result.lon);
    setSelectedPos({ lat, lng, zoom: 16, address: result.display_name });
  };

  // 지도 클릭 시 호출되는 함수: reverse geocoding 수행하여 주소 가져오기
  const handleMapClick = async (lat: number, lng: number, zoom: number) => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`
      );
      const data = await response.json();
      const address = data && data.display_name ? data.display_name : "";
      setSelectedPos({ lat, lng, zoom, address });
    } catch (error) {
      console.error("Reverse geocoding 오류:", error);
      setSelectedPos({ lat, lng, zoom, address: "" });
    }
  };

  // "선택 완료" 버튼 클릭 시 부모로 좌표와 address 전달 후 팝업 종료
  const handleConfirm = () => {
    if (selectedPos) {
      onLocationSelect(
        selectedPos.lat,
        selectedPos.lng,
        selectedPos.zoom,
        selectedPos.address
      );
      onClose();
    } else {
      alert("좌표를 먼저 선택해 주세요.");
    }
  };

  return (
    <div className="absolute top-0 left-0 right-0 bottom-0 z-50 flex items-center justify-center bg-black bg-opacity-30">
      <div className="bg-white p-4 rounded shadow-lg w-[80%] h-[80%] relative flex flex-col">
        {/* 검색 영역 */}
        <div className="mb-4">
          <div className="flex items-center">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="장소 검색..."
              className="border p-2 flex-grow"
            />
            <button
              type="button"
              onClick={handleSearch}
              className="ml-2 bg-blue-500 text-white p-2 rounded"
            >
              검색
            </button>
            <button
              type="button"
              onClick={onClose}
              className="ml-2 bg-gray-200 p-2 rounded"
            >
              닫기
            </button>
          </div>
          {/* 검색 결과 리스트 */}
          {searchResults.length > 0 && (
            <div className="mt-2 max-h-40 overflow-y-auto border p-2">
              {searchResults.map((result, idx) => (
                <div
                  key={idx}
                  className="p-1 hover:bg-gray-100 cursor-pointer"
                  onClick={() => handleResultClick(result)}
                >
                  {result.display_name}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 지도 영역 */}
        <div className="flex-grow">
          <MapWithMarkers
            currentPos={
              selectedPos
                ? {
                    lat: selectedPos.lat,
                    lng: selectedPos.lng,
                    zoom: selectedPos.zoom,
                  }
                : currentPos
            }
            onLocationSelect={(lat, lng, zoom) => {
              // 지도 클릭 시 reverse geocoding을 수행하여 주소를 업데이트
              handleMapClick(lat, lng, zoom);
            }}
          />
        </div>

        {/* 선택 완료 버튼 */}
        <div className="mt-4 flex justify-end">
          <button
            type="button"
            onClick={handleConfirm}
            className="bg-green-500 text-white p-2 rounded"
          >
            선택 완료
          </button>
        </div>
      </div>
    </div>
  );
}
