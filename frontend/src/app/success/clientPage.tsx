"use client";

import client from "@/lib/client";
import { useRouter } from "next/navigation";
import { useContext, useEffect } from "react";
import { LoginMemberContext } from "../stores/auth/loginMemberStore";

export default function ClientPage({
  orderId,
  paymentKey,
  amount,
}: {
  orderId: string;
  paymentKey: string;
  amount: number;
}) {
  const loginMemberContextValue = useContext(LoginMemberContext);
  const router = useRouter();
  const requestPayment = async () => {
    const response = await client.GET("/api/payments/request", {
      params: {
        query: {
          amount,
          orderId,
          paymentKey,
        },
      },
      credentials: "include",
    });
    if (response.error) {
      console.log(response.error);
    }
  };

  const updateUser = async () => {
    const response = await client.GET("/api/users/me", {
      credentials: "include",
    });
    if (response.error) {
      console.log(response.error);
      return;
    }

    loginMemberContextValue.setLoginMember(response.data.data);
  };
  useEffect(() => {
    requestPayment();
    updateUser();
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
