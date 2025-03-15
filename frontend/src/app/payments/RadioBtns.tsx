"use client";

import * as React from "react";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";

export type PaymentOption = { amount: string; id: string; displayName: string };

export default function CustomRadioGroup({
  options,
  setSelectedValue,
}: {
  options: PaymentOption[];
  setSelectedValue: React.Dispatch<React.SetStateAction<number>>;
}) {
  const [selected, setSelected] = React.useState<string>("option1");
  console.log(options);
  return (
    <RadioGroup
      defaultValue={selected}
      onValueChange={(value) => {
        setSelected(value);
        setSelectedValue(Number(value));
      }}
      className="flex flex-col space-y-4 "
    >
      {options.map((option, idx) => {
        return (
          <div className="flex items-center space-x-2" key={idx}>
            <RadioGroupItem
              value={option.amount}
              className="border-4 w-5 h-5"
              id={option.id}
            ></RadioGroupItem>
            <Label className="text-3xl" htmlFor={option.id}>
              {option.displayName}
            </Label>
          </div>
        );
      })}
    </RadioGroup>
  );
}
