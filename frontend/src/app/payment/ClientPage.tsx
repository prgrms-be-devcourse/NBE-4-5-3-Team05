"use client";

import { useState } from "react";
import CheckoutButton from "./CheckoutButton";
import { v4 as uuidv4 } from "uuid";
import client from "@/lib/client";

export default function CheckoutPage() {
  const values = [1000, 5000, 10000, 50000];
  // 선택된 값을 관리하는 state (초기값은 null 또는 원하는 기본값)
  const [selectedValue, setSelectedValue] = useState<number>(0);
  const [productId, setProductId] = useState<string>("");

  const purchaseItem = async () => {
    const response = await client.POST("/api/payments", {
      body: {
        productId: productId,
      },
    });
    if (response.error) {
      console.log(response.error);
      return;
    }
  };
  return (
    <div>
      <div>
        {values.map((value) => (
          <label key={value} style={{ display: "block", marginBottom: "8px" }}>
            <input
              type="radio"
              name="amount"
              value={value}
              checked={selectedValue === value}
              onChange={(e) => {
                console.log(e.target.value);
                setSelectedValue(Number(e.target.value));
              }}
            />
            {value}원
          </label>
        ))}
        <CheckoutButton
          orderId={"payment-" + uuidv4()}
          totalAmount={selectedValue}
        ></CheckoutButton>
      </div>
      <div>상품 구매</div>
      <input
        value={productId}
        onChange={(e) => {
          e.preventDefault();
          setProductId(e.target.value);
        }}
      />
      <input
        type="button"
        value={"구매"}
        onClick={(e) => {
          e.preventDefault();
          purchaseItem();
        }}
      />
    </div>
  );
}
