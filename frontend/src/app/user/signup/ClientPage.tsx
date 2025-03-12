"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function ClientPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
    nickname: "",
    address: "",
    profileUrl: "",
  });

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  }

  async function join(e: React.FormEvent) {
    e.preventDefault();

    // 필수 입력값 검증
    if (!formData.username.trim()) {
      alert("아이디를 입력해주세요.");
      return;
    }
    if (!formData.password.trim()) {
      alert("비밀번호를 입력해주세요.");
      return;
    }
    if (!formData.email.trim()) {
      alert("이메일을 입력해주세요.");
      return;
    }

    const response = await client.POST("/api/users/signup", {
      body: formData,
      credentials: "include",
    });

    if (response.error) {
      alert(response.error.message);
      return;
    }

    alert("회원가입에 성공하였습니다.");
    router.push("/user/login");
  }

  return (
    <>
      <div className="flex items-center justify-center h-full">
        <div className="flex flex-col gap-2">
          <h2 className="text-xl font-bold text-center">회원가입</h2>
          <form onSubmit={join} className="flex flex-col w-[350px] gap-2">
            <Input
              type="text"
              name="username"
              placeholder="아이디"
              className="border-2 border-black"
              value={formData.username}
              onChange={handleChange}
              required
            />
            <Input
              type="password"
              name="password"
              placeholder="비밀번호"
              className="border-2 border-black"
              value={formData.password}
              onChange={handleChange}
              required
            />
            <Input
              type="email"
              name="email"
              placeholder="이메일"
              className="border-2 border-black"
              value={formData.email}
              onChange={handleChange}
              required
            />
            <Input
              type="text"
              name="nickname"
              placeholder="닉네임"
              className="border-2 border-black"
              value={formData.nickname}
              onChange={handleChange}
              required
            />
            <Input
              type="text"
              name="address"
              placeholder="주소"
              className="border-2 border-black w-[500px]"
              value={formData.address}
              onChange={handleChange}
            />
            <Input
              type="url"
              name="profileUrl"
              placeholder="프로필URL"
              className="border-2 border-black w-[500px]"
              value={formData.profileUrl}
              onChange={handleChange}
            />
            <Button
              type="submit"
              className={`p-2 ${
                formData.username &&
                formData.password &&
                formData.email &&
                formData.nickname
                  ? "bg-blue-500 text-white"
                  : "bg-gray-300 text-gray-500 cursor-not-allowed"
              }`}
              disabled={
                !formData.username ||
                !formData.password ||
                !formData.email ||
                !formData.nickname
              }
            >
              회원가입
            </Button>
          </form>
        </div>
      </div>
    </>
  );
}
