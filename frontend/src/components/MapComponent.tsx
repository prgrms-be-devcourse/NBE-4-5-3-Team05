// MapWithMarkers.tsx
"use client";

import React, { memo, useCallback, useEffect, useState } from "react";
import "leaflet/dist/leaflet.css";
import L, { LatLng, LatLngExpression } from "leaflet";
import dynamic from "next/dynamic";
import { useMapEvents } from "react-leaflet/hooks";
import ChangeView from "./ChangeView";

// Props 인터페이스 정의
export interface MapWithMarkersProps {
  onLocationSelect: (lat: number, lng: number, zoom: number) => void;
  currentPos: { lat: number; lng: number; zoom: number };
}

// 마커 아이콘 설정 (이미지 파일 경로, 크기, 앵커는 필요에 따라 조정)
const customIcon = L.icon({
  iconUrl: "/map/marker-icon.png",
  iconSize: [50, 50],
  iconAnchor: [25, 50], // 마커의 바닥 중앙 기준
});

// react-leaflet 컴포넌트 동적 임포트 (SSR 비활성화)
const MapContainer = dynamic(
  () => import("react-leaflet").then((mod) => mod.MapContainer),
  { ssr: false }
);
const TileLayer = dynamic(
  () => import("react-leaflet").then((mod) => mod.TileLayer),
  { ssr: false }
);
const Marker = dynamic(
  () => import("react-leaflet").then((mod) => mod.Marker),
  { ssr: false }
);

// 줌 변경 이벤트 처리 컴포넌트
function ZoomTracker({
  onZoomChange,
}: {
  onZoomChange: (zoom: number) => void;
}) {
  useMapEvents({
    zoomend(e) {
      const newZoom = e.target.getZoom();
      onZoomChange(newZoom);
    },
  });
  return null;
}

// 지도 클릭 이벤트 처리 컴포넌트
function MapClickHandler({
  onLocationSelect,
}: {
  onLocationSelect: (lat: number, lng: number, zoom: number) => void;
}) {
  useMapEvents({
    click(e) {
      const newMarker = e.latlng;
      onLocationSelect(newMarker.lat, newMarker.lng, e.target.getZoom());
    },
  });
  return null;
}

const MapWithMarkers = memo(function MapWithMarkers({
  onLocationSelect,
  currentPos,
}: MapWithMarkersProps) {
  const [markers, setMarkers] = useState<LatLng[]>([]);
  // view 상태: 지도 center와 zoom 값을 관리
  const [view, setView] = useState({
    center: [currentPos.lat, currentPos.lng] as LatLngExpression,
    zoom: currentPos.zoom,
  });

  useEffect(() => {
    setMarkers([new LatLng(currentPos.lat, currentPos.lng)]);
    setView({
      center: [currentPos.lat, currentPos.lng],
      zoom: currentPos.zoom,
    });
  }, [currentPos.lat, currentPos.lng, currentPos.zoom]);

  const handleMapClick = useCallback(
    (lat: number, lng: number, zoom: number) => {
      setMarkers([new LatLng(lat, lng)]);
      setView({ center: [lat, lng], zoom });
      onLocationSelect(lat, lng, zoom);
    },
    [onLocationSelect]
  );

  const handleZoomChange = useCallback((zoom: number) => {
    setView((prev) => ({ ...prev, zoom }));
  }, []);

  return (
    <div className="w-full h-full">
      <MapContainer
        center={view.center}
        zoom={view.zoom}
        style={{ width: "100%", height: "100%" }}
      >
        {/* ChangeView 컴포넌트로 지도 뷰 업데이트 */}
        <ChangeView center={view.center} zoom={view.zoom} />
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution="&copy; OpenStreetMap contributors"
          maxZoom={20}
        />
        <MapClickHandler onLocationSelect={handleMapClick} />
        {markers.map((position, idx) => (
          <Marker key={idx} position={position} icon={customIcon} />
        ))}
        <ZoomTracker onZoomChange={handleZoomChange} />
      </MapContainer>
    </div>
  );
});

export default MapWithMarkers;
