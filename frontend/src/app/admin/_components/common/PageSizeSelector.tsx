import { useState } from "react";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { ChevronDown } from "lucide-react";

const pageSizes = [5, 10, 15, 20, 25, 50];
interface PageSizeSelectorProps {
  selectedPageSize: number;
  setSelectedPageSize: (size: number) => void;
}
export default function PageSizeSelector({
  selectedPageSize,
  setSelectedPageSize,
}: PageSizeSelectorProps) {
  return (
    <div className="max-w-md p-4 border rounded-lg shadow-md">
      <label className="text-lg font-semibold">Select Page Size:</label>
      <Select onValueChange={(value) => setSelectedPageSize(Number(value))}>
        <SelectTrigger className="w-full mt-2">
          <SelectValue placeholder={`${selectedPageSize} items per page`} />
        </SelectTrigger>
        <SelectContent>
          {pageSizes.map((size) => (
            <SelectItem key={size} value={String(size)}>
              {size} items per page
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
