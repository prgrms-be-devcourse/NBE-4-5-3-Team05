import { useEffect, useState } from "react";
import "leaflet/dist/leaflet.css";
import L, { LatLng } from "leaflet";
import dynamic from "next/dynamic";
import { useMapEvents } from "react-leaflet/hooks";

interface MapWithMarkersProps {
  onLocationSelect: (lat: number, lng: number, zoom: number) => void;
  currentPos: { lat: number; lng: number; zoom: number };
}

const customIcon = L.icon({
  iconUrl: "/map/marker-icon.png",
  // 가로(예: 40px), 세로(예: 50px)
  iconSize: [50, 50],
  iconAnchor: [25, 0], // 마커의 바닥(고정점)을 정 가운데로 맞추기
});

export default function MapWithMarkers({
  onLocationSelect,
  currentPos,
}: MapWithMarkersProps) {
  console.log(currentPos);
  const [markers, setMarkers] = useState<LatLng[]>([]);
  let zoom = 15;

  const MapContainer = dynamic(
    () => import("react-leaflet").then((module) => module.MapContainer),
    {
      ssr: false, // Disable server-side rendering for this component
    }
  );
  const TileLayer = dynamic(
    () => import("react-leaflet").then((module) => module.TileLayer),
    {
      ssr: false,
    }
  );
  const Marker = dynamic(
    () => import("react-leaflet").then((module) => module.Marker),
    {
      ssr: false,
    }
  );
  const Popup = dynamic(
    () => import("react-leaflet").then((module) => module.Popup),
    {
      ssr: false,
    }
  );
  function ZoomTracker({
    onZoomChange,
  }: {
    onZoomChange: (zoom: number) => void;
  }) {
    useMapEvents({
      zoomend(e) {
        const currentZoom = e.target.getZoom();
        onZoomChange(currentZoom);
      },
    });
    return null;
  }
  function MapClickHandler() {
    useMapEvents({
      click(e) {
        const newMarker = e.latlng;
        setMarkers([newMarker]); // 마지막 클릭 위치만 저장
        console.log(newMarker);
        onLocationSelect(newMarker.lat, newMarker.lng, zoom); // 부모 컴포넌트로 좌표 전달
      },
    });
    return null;
  }

  useEffect(() => {
    setMarkers([new LatLng(currentPos.lat, currentPos.lng)]);
  }, []);
  return (
    <div className=" w-full h-full">
      <MapContainer
        center={[currentPos.lat, currentPos.lng]}
        zoom={currentPos.zoom}
        className="h-full w-full"
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        <MapClickHandler />
        {markers.map((position, idx) => (
          <Marker key={idx} position={position} icon={customIcon} />
        ))}
        <ZoomTracker
          onZoomChange={(changedZoom) => {
            zoom = changedZoom;
          }}
        ></ZoomTracker>
      </MapContainer>
    </div>
  );
}
