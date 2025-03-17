"use client";

import client from "@/lib/client";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function ClientPage({
  orderId,
  paymentKey,
  amount,
}: {
  orderId: string;
  paymentKey: string;
  amount: number;
}) {
  const router = useRouter();
  const requestPayment = async () => {
    let baseUrl = `${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_BACKEND_HOST}`;
    if (`${process.env.NEXT_PUBLIC_PROTOCOL}` === "https") {
      baseUrl += `/api/payments`;
    } else {
      baseUrl += `:${process.env.NEXT_PUBLIC_BACKEND_PORT}/api/payments`;
    }
    const response = await fetch(baseUrl, {
      credentials: "include",
    });
    const json = await response.json();

    console.log(json);
  };
  useEffect(() => {
    requestPayment();
    router.push("/");
  }, []);
  return (
    <div
      onLoad={(e) => {
        e.preventDefault();
      }}
    ></div>
  );
}
