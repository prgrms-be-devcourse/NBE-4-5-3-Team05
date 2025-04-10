"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
import { useRouter } from "next/navigation";
import { useState, ChangeEvent, FormEvent } from "react";

export default function AdminSignUp() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
    nickname: "",
  });

  const [isEmailSent, setIsEmailSent] = useState(false);
  const [emailCode, setEmailCode] = useState("");
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [emailStatus, setEmailStatus] = useState("");
  const [emailStatusColor, setEmailStatusColor] = useState("text-red-500");
  const [codeStatus, setCodeStatus] = useState("");
  const [codeStatusColor, setCodeStatusColor] = useState("text-red-500");
  const [isCodeLoading, setIsCodeLoading] = useState(false);

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  }

  async function sendEmailVerification() {
    if (!formData.email.trim()) {
      alert("이메일을 입력해주세요.");
      return;
    }
    setIsLoading(true);
    setEmailStatus("인증 코드 발송 중입니다. 잠시만 기다려 주세요");
    setEmailStatusColor("text-red-500");
    setIsEmailSent(true);

    const response = await client.POST("/api/users/email/code", {
      body: { email: formData.email },
    });
    setIsLoading(false);
    if (response.error) {
      alert(response.error.message);
      setIsEmailSent(false);
      setEmailStatus("");
      return;
    }
    setEmailStatus("인증 코드가 발송되었습니다.");
    setEmailStatusColor("text-blue-500");
  }

  async function verifyEmailCode() {
    if (!emailCode.trim()) {
      alert("인증 코드를 입력해주세요.");
      return;
    }
    setIsCodeLoading(true);
    const response = await client.POST("/api/users/email/code/verify", {
      body: { email: formData.email, code: emailCode },
    });
    setIsCodeLoading(false);
    if (response.error) {
      setCodeStatus(response.error.message);
      setCodeStatusColor("text-red-500");
      return;
    }
    setCodeStatus("이메일 인증이 완료되었습니다.");
    setCodeStatusColor("text-blue-500");
    setIsEmailVerified(true);
  }

  async function join(e: FormEvent) {
    e.preventDefault();

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
    if (!formData.nickname.trim()) {
      alert("닉네임을 입력해주세요.");
      return;
    }
    if (!isEmailVerified) {
      alert("이메일 인증을 완료해주세요.");
      return;
    }

    const response = await client.POST("/api/admin/signup", {
      body: formData,
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    });

    if (response.error) {
      alert(response.error.message);
      return;
    }
    alert("관리자 회원가입에 성공하였습니다.");
    router.push("/admin");
  }

  return (
    <div className="flex items-center justify-center min-h-screen">
      <form onSubmit={join} className="flex flex-col gap-8 w-full max-w-4xl">
        <h2 className="text-3xl font-bold text-center">관리자 회원가입</h2>
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
        <div className="flex flex-col gap-1">
          <div className="flex gap-2 items-center">
            <Input
              type="email"
              name="email"
              placeholder="이메일"
              className="border-2 border-black flex-grow"
              value={formData.email}
              onChange={handleChange}
              required
            />
            <Button
              type="button"
              className="bg-blue-500 text-white p-2 flex items-center justify-center"
              onClick={sendEmailVerification}
              disabled={isLoading}
            >
              {isLoading ? (
                <div className="animate-spin border-2 border-white border-t-transparent w-5 h-5 rounded-full"></div>
              ) : (
                "이메일 인증"
              )}
            </Button>
          </div>
          {emailStatus && (
            <p className={`text-sm ${emailStatusColor}`}>{emailStatus}</p>
          )}
        </div>
        {isEmailSent && (
          <div className="flex flex-col gap-1">
            <div className="flex gap-2 items-center">
              <Input
                type="text"
                placeholder="인증 코드"
                className="border-2 border-black flex-grow"
                value={emailCode}
                onChange={(e) => setEmailCode(e.target.value)}
              />
              <Button
                type="button"
                className="bg-green-500 text-white p-2 flex items-center justify-center"
                onClick={verifyEmailCode}
                disabled={isCodeLoading}
              >
                {isCodeLoading ? (
                  <div className="animate-spin border-2 border-white border-t-transparent w-5 h-5 rounded-full"></div>
                ) : (
                  "확인"
                )}
              </Button>
            </div>
            {codeStatus && (
              <p className={`text-sm ${codeStatusColor}`}>{codeStatus}</p>
            )}
          </div>
        )}
        <Input
          type="text"
          name="nickname"
          placeholder="닉네임"
          className="border-2 border-black"
          value={formData.nickname}
          onChange={handleChange}
          required
        />
        <Button
          type="submit"
          className={`w-full p-4 ${
            formData.username &&
            formData.password &&
            formData.email &&
            formData.nickname &&
            isEmailVerified
              ? "bg-blue-500 text-white"
              : "bg-gray-300 text-gray-500 cursor-not-allowed"
          }`}
          disabled={
            !formData.username ||
            !formData.password ||
            !formData.email ||
            !formData.nickname ||
            !isEmailVerified
          }
        >
          관리자 회원가입
        </Button>
      </form>
    </div>
  );
}
