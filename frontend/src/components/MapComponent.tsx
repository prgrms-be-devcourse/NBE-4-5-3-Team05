import { useState } from "react";
import { MapContainer, TileLayer, Marker, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { LatLng } from "leaflet";

interface MapWithMarkersProps {
  onLocationSelect: (lat: number, lng: number) => void;
}

export default function MapWithMarkers({
  onLocationSelect,
}: MapWithMarkersProps) {
  const [markers, setMarkers] = useState<LatLng[]>([]);

  function MapClickHandler() {
    useMapEvents({
      click(e) {
        const newMarker = e.latlng;
        setMarkers([newMarker]); // 마지막 클릭 위치만 저장
        console.log(newMarker);
        onLocationSelect(newMarker.lat, newMarker.lng); // 부모 컴포넌트로 좌표 전달
      },
    });
    return null;
  }

  return (
    <div className="h-screen w-full">
      <MapContainer
        center={[37.7749, -122.4194]}
        zoom={13}
        className="h-full w-full"
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        <MapClickHandler />
        {markers.map((position, idx) => (
          <Marker key={idx} position={position} />
        ))}
      </MapContainer>
    </div>
  );
}
