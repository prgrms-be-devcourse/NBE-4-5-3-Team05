import { useState } from "react";
import { MapContainer, TileLayer, Marker, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { LatLng } from "leaflet";

export default function MapWithMarkers() {
  const [markers, setMarkers] = useState<LatLng[]>([]);

  function MapClickHandler() {
    useMapEvents({
      click(e) {
        setMarkers((prev) => [...prev, e.latlng]);
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
