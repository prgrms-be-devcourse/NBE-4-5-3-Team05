"use client";

import React, { useState } from "react";

interface FilterSidebarProps {
  onFilterChange: (filters: {
    keyword: string;
    minPrice: number | null;
    maxPrice: number | null;
    categories: string[];
    sort: string;
  }) => void;
  categories: { id: number; name: string }[];
}

export default function FilterSidebar({
  onFilterChange,
  categories,
}: FilterSidebarProps) {
  const [keyword, setKeyword] = useState("");
  const [minPrice, setMinPrice] = useState<string>("");
  const [maxPrice, setMaxPrice] = useState<string>("");
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [sort, setSort] = useState("desc");

  const handleCategoryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (e.target.checked) {
      setSelectedCategories((prev) => [...prev, value]);
    } else {
      setSelectedCategories((prev) => prev.filter((cat) => cat !== value));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onFilterChange({
      keyword,
      minPrice: minPrice.trim() === "" ? null : Number(minPrice),
      maxPrice: maxPrice.trim() === "" ? null : Number(maxPrice),
      categories: selectedCategories,
      sort,
    });
  };

  return (
    <aside className="w-64 p-4 border-r border-gray-200">
      <h2 className="text-lg font-semibold mb-4">상세 검색</h2>
      <form onSubmit={handleSubmit} className="space-y-3">
        <div>
          <label className="block mb-1">키워드</label>
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            className="w-full border rounded p-1"
          />
        </div>
        <div>
          <label className="block mb-1">최소 금액</label>
          <input
            type="number"
            min="0"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            className="w-full border rounded p-1"
          />
        </div>
        <div>
          <label className="block mb-1">최대 금액</label>
          <input
            type="number"
            min="0"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            className="w-full border rounded p-1"
          />
        </div>
        <div>
          <label className="block mb-1">카테고리</label>
          <div className="flex flex-wrap gap-2">
            {categories.map((cat) => (
              <label
                key={cat.id}
                className="flex items-center space-x-2 border p-2 rounded-md cursor-pointer"
              >
                <input
                  type="checkbox"
                  value={cat.name}
                  onChange={handleCategoryChange}
                />
                <span>{cat.name}</span>
              </label>
            ))}
          </div>
        </div>
        <div>
          <label className="block mb-1">정렬</label>
          <select
            value={sort}
            onChange={(e) => setSort(e.target.value)}
            className="w-full border rounded p-1"
          >
            <option value="desc">최신순</option>
            <option value="asc">오래된 순</option>
          </select>
        </div>
        <button
          type="submit"
          className="w-full bg-blue-500 text-white rounded p-2"
        >
          검색
        </button>
      </form>
    </aside>
  );
}
