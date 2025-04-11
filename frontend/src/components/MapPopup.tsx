// MapPopup.tsx
"use client";

import React, { useCallback, useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTimes } from "@fortawesome/free-solid-svg-icons";
import MapWithMarkers from "./MapComponent";

// 검색 결과 타입 정의
interface SearchResult {
  lat: string;
  lon: string;
  display_name: string;
}

// MapPopupProps 인터페이스: 초기 위치, 위치 선택 콜백, 모달 닫기 콜백
export interface MapPopupProps {
  currentPos: { lat: number; lng: number; zoom: number };
  onLocationSelect: (
    lat: number,
    lng: number,
    zoom: number,
    address: string
  ) => void;
  onClose: () => void;
}

/**
 * MapPopup 컴포넌트는 검색어 입력, 검색 결과 리스트, 지도 표시 기능을 포함합니다.
 * 사용자가 검색 결과를 클릭하면 해당 좌표와 최대 확대(zoom=18) 상태로 마커를 찍고,
 * "선택 완료" 버튼 클릭 시 부모로 좌표와 주소를 전달합니다.
 */
const MapPopup: React.FC<MapPopupProps> = ({
  currentPos,
  onLocationSelect,
  onClose,
}) => {
  const [searchQuery, setSearchQuery] = useState(""); // 검색어 상태
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]); // 검색 결과 목록
  const [selectedPos, setSelectedPos] = useState<{
    lat: number;
    lng: number;
    zoom: number;
    address: string;
  } | null>(null);
  const [loadingLocation, setLoadingLocation] = useState(false); // 역지오코딩 로딩 상태

  // 검색 API 호출: Nominatim API 사용
  const handleSearch = async () => {
    if (!searchQuery) return;
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
          searchQuery
        )}`
      );
      const results: SearchResult[] = await response.json();
      if (results.length > 0) {
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

  // 검색 결과 클릭 시 호출: 선택된 좌표를 최대 확대 줌(18)으로 설정
  const handleResultClick = (result: SearchResult) => {
    const lat = parseFloat(result.lat);
    const lng = parseFloat(result.lon);
    const desiredZoom = 18; // 최대 확대 줌값으로 설정
    setSelectedPos({
      lat,
      lng,
      zoom: desiredZoom,
      address: result.display_name,
    });
  };

  // 지도 클릭 시 reverse geocoding 호출: 선택된 좌표를 업데이트
  const handleMapClick = async (lat: number, lng: number, zoom: number) => {
    setLoadingLocation(true);
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`
      );
      const data = await response.json();
      const address = data?.display_name || "";
      setSelectedPos({ lat, lng, zoom, address });
    } catch (error) {
      console.error("Reverse geocoding 오류:", error);
      setSelectedPos({ lat, lng, zoom, address: "" });
    } finally {
      setLoadingLocation(false);
    }
  };

  // 선택 완료 버튼 클릭: 선택된 좌표와 주소를 부모 컴포넌트에 전달하고 모달 닫기
  const handleConfirm = useCallback(() => {
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
  }, [selectedPos, onLocationSelect, onClose]);

  return (
    <div className="absolute inset-0 z-50 flex items-center justify-center bg-black bg-opacity-30">
      <div className="bg-white p-4 rounded shadow-lg w-[80%] h-[80%] relative flex flex-col">
        {/* 모달 헤더 */}
        <div className="flex justify-between items-center p-4 border-b">
          <h2 className="text-lg font-bold">거래 위치 보기</h2>
          <button
            onClick={onClose}
            className="text-gray-600 hover:text-gray-800"
          >
            <FontAwesomeIcon icon={faTimes} size="lg" />
          </button>
        </div>
        {/* 검색 영역 */}
        <div className="mb-4">
          <div className="flex items-center">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="장소 검색..."
              className="border p-2 flex-grow"
              // 엔터키로 검색하면 폼 제출을 막고 handleSearch 호출
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  handleSearch();
                }
              }}
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
        <div className="flex-grow relative" style={{ height: "100%" }}>
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
              handleMapClick(lat, lng, zoom);
            }}
          />
        </div>
        {/* 하단 영역 */}
        <div className="mt-4 flex justify-end">
          {loadingLocation ? (
            <div>로딩중...</div>
          ) : (
            <button
              type="button"
              onClick={handleConfirm}
              className="bg-green-500 text-white p-2 rounded"
            >
              선택 완료
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default MapPopup;
