"use client";

import { useEffect } from "react";
import { useMap } from "react-leaflet/hooks";
import { LatLngExpression } from "leaflet";

interface ChangeViewProps {
  center: LatLngExpression;
  zoom: number;
}

export default function ChangeView({ center, zoom }: ChangeViewProps) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, zoom);
  }, [center, zoom, map]);
  return null;
}
