"use client";

import { useEffect, useState } from "react";
import CheckoutButton from "./CheckoutButton";
import { v4 as uuidv4 } from "uuid";
import RadioBtns, { PaymentOption } from "./RadioBtns";

export default function CheckoutPage() {
  const values = [1000, 5000, 10000, 50000];
  // 선택된 값을 관리하는 state (초기값은 null 또는 원하는 기본값)
  const [selectedValue, setSelectedValue] = useState<number>(0);
  const [productId, setProductId] = useState<string>("");
  const [paymentOptions, setPaymentOptions] = useState<PaymentOption[]>([]);

  const purchaseItem = async () => {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_BACKEND_HOST}:${process.env.NEXT_PUBLIC_BACKEND_PORT}/api/payments`,
      {
        method: "POST",
        body: JSON.stringify({
          productId: productId,
        }),
        headers: {
          "Content-Type": "application/json",
        },
      }
    );
    const json = await response.json();
    console.log(json);
  };

  useEffect(() => {
    const getPaymentOptions = async () => {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_BACKEND_HOST}:${process.env.NEXT_PUBLIC_BACKEND_PORT}/paymentOptions.json`
      );

      const data = await response.json();

      setPaymentOptions(data);
    };
    getPaymentOptions();
  }, []);
  return (
    <div className="flex-1 flex w-full justify-center">
      <div className="flex flex-1 flex-col w-1/2 justify-center items-center">
        <div className="flex flex-col border justify-between rounded-3xl w-1/2 h-3/4 p-20 items-center ">
          <div>
            <p className="text-5xl font-bold">결제할 금액</p>
          </div>
          <div className="flex ">
            <RadioBtns
              setSelectedValue={setSelectedValue}
              options={paymentOptions}
            ></RadioBtns>
          </div>
          <div className="w-full">
            <CheckoutButton
              orderId={"payment-" + uuidv4()}
              totalAmount={selectedValue}
            ></CheckoutButton>
          </div>
        </div>
      </div>
    </div>
  );
}
