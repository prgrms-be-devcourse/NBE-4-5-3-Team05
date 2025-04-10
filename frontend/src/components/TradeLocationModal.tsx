"use client";

import React from "react";
import dynamic from "next/dynamic";
import "leaflet/dist/leaflet.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTimes } from "@fortawesome/free-solid-svg-icons";

interface TradeLocationModalProps {
  lat: number;
  lng: number;
  zoom: number;
  address: string;
  onClose: () => void;
}

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

import L from "leaflet";
const customIcon = L.icon({
  iconUrl: "/map/marker-icon.png",
  iconSize: [50, 50],
  iconAnchor: [25, 50],
});

export default function TradeLocationModal({
  lat,
  lng,
  zoom,
  address,
  onClose,
}: TradeLocationModalProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
      <div className="relative bg-white rounded shadow-lg w-[90%] max-w-3xl h-[70%]">
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
        {/* 지도 영역 */}
        <div className="flex-grow relative" style={{ height: "100%" }}>
          <MapContainer
            center={[lat, lng]}
            zoom={zoom}
            style={{ width: "100%", height: "100%" }}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution="&copy; OpenStreetMap contributors"
            />
            <Marker position={[lat, lng]} icon={customIcon} />
          </MapContainer>
        </div>
        {/* 하단 정보 영역 */}
        <div className="p-4 border-t">
          <p className="text-sm text-gray-700">
            선택된 거래위치: <strong>{address}</strong> (위도: {lat}, 경도:{" "}
            {lng})
          </p>
        </div>
      </div>
    </div>
  );
}
