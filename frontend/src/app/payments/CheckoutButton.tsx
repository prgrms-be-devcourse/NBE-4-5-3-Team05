"use client";

import { Button } from "@/components/ui/button";
import {
  loadTossPayments,
  TossPaymentsPayment,
} from "@tosspayments/tosspayments-sdk";
import { useEffect, useState } from "react";
// ------  SDK 초기화 ------
// @docs https://docs.tosspayments.com/sdk/v2/js#토스페이먼츠-초기화
const clientKey = "test_ck_kYG57Eba3G6jzE0Rmam58pWDOxmA";
const customerKey = "Ud88YCHjvIQZ1WYPnBKHW";

export default function CheckoutButton({
  orderId,
  totalAmount,
}: {
  orderId: string;
  totalAmount: number;
}) {
  console.log("in checkoutBtn", totalAmount);
  const [payment, setPayment] = useState<TossPaymentsPayment>();
  const [amount, setAmount] = useState({
    currency: "KRW",
    value: totalAmount,
  });
  // const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(null);
  // function selectPaymentMethod(method) {
  //   setSelectedPaymentMethod(method);
  // }
  useEffect(() => {
    async function fetchPayment() {
      try {
        const tossPayments = await loadTossPayments(clientKey);
        // 회원 결제
        // @docs https://docs.tosspayments.com/sdk/v2/js#tosspaymentspayment
        const payment = tossPayments.payment({
          customerKey,
        });
        // 비회원 결제
        // const payment = tossPayments.payment({ customerKey: ANONYMOUS });
        setPayment(payment);
      } catch (error) {
        console.error("Error fetching payment:", error);
      }
    }
    fetchPayment();
  }, [clientKey, customerKey]);

  useEffect(() => {
    setAmount({
      value: totalAmount,
      currency: "KRW",
    });
  }, [totalAmount]);
  // ------ '결제하기' 버튼 누르면 결제창 띄우기 ------
  // @docs https://docs.tosspayments.com/sdk/v2/js#paymentrequestpayment
  async function requestPayment() {
    let url = "";
    if (`${process.env.NEXT_PUBLIC_PROTOCOL}` === "https") {
      url = `${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_BACKEND_HOST}/api/payments/metadata?id=${orderId}&amount=${amount.value}`;
    } else {
      url = `${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_BACKEND_HOST}:${process.env.NEXT_PUBLIC_BACKEND_PORT}/api/payments/metadata?id=${orderId}&amount=${amount.value}`;
    }
    const result = await fetch(url, {
      credentials: "include",
    });
    console.log("amount:", amount);
    // 결제를 요청하기 전에 orderId, amount를 서버에 저장하세요.
    // 결제 과정에서 악의적으로 결제 금액이 바뀌는 것을 확인하는 용도입니다.
    await payment?.requestPayment({
      method: "CARD", // 카드 및 간편결제
      amount: amount,
      orderId: orderId, // 고유 주문번호
      orderName: "토스 티셔츠 외 2건",
      successUrl: window.location.origin + "/success", // 결제 요청이 성공하면 리다이렉트되는 URL
      failUrl: window.location.origin + "/fail", // 결제 요청이 실패하면 리다이렉트되는 URL
      customerEmail: "customer123@gmail.com",
      customerName: "김토스",
      customerMobilePhone: "01012341234",
      // 카드 결제에 필요한 정보
      card: {
        useEscrow: false,
        flowMode: "DEFAULT", // 통합결제창 여는 옵션
        useCardPoint: false,
        useAppCardOnly: false,
      },
    });
  }
  return (
    // 결제하기 버튼
    <Button
      className="w-full h-full button text-3xl"
      onClick={() => requestPayment()}
    >
      결제하기
    </Button>
  );
}
