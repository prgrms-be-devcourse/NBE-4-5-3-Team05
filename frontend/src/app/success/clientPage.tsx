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
    const response = await client.GET("/api/payments/request", {
      params: {
        query: {
          orderId,
          paymentKey,
          amount,
        },
      },
      credentials: "include",
    });
    if (response.error) {
      console.log(response.error);
    }
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
