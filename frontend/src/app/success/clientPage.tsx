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
    const response = await fetch(
      `http://${process.env.NEXT_PUBLIC_BACKEND_HOST}:${process.env.NEXT_PUBLIC_BACKEND_PORT}/api/payments/request?orderId=${orderId}&paymentKey=${paymentKey}&amount=${amount}`,
      {
        credentials: "include",
      }
    );
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
