"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
import fileUploadClient from "@/lib/fileUploadClient";
import { useRouter } from "next/navigation";
import { useState, ChangeEvent, FormEvent } from "react";

export default function ClientPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
    nickname: "",
    address: "",
    profileUrl: "", // 업로드 후 반환받은 프로필 이미지 URL
  });

  // 단일 프로필 파일 상태
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const [isEmailSent, setIsEmailSent] = useState(false);
  const [emailCode, setEmailCode] = useState("");
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [emailStatus, setEmailStatus] = useState("");
  const [emailStatusColor, setEmailStatusColor] = useState("text-red-500");
  const [codeStatus, setCodeStatus] = useState("");
  const [codeStatusColor, setCodeStatusColor] = useState("text-red-500");
  const [isCodeLoading, setIsCodeLoading] = useState(false);

  // 기본 이미지 URL (파일 선택 없을 경우)
  const defaultImageUrl = "http://localhost:8080/images/default_profile.jpg";

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  }

  // 파일 입력 핸들러: 선택한 파일을 저장
  function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    if (e.target.files && e.target.files.length > 0) {
      setSelectedFile(e.target.files[0]);
    }
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
    const response = await client.POST("/api/users/email/code/verify", {
      body: { email: formData.email, code: emailCode },
    });
    if (response.error) {
      setCodeStatus(response.error.message);
      setCodeStatusColor("text-red-500");
      return;
    }
    setCodeStatus("이메일 인증이 완료되었습니다.");
    setCodeStatusColor("text-blue-500");
    setIsEmailVerified(true);
  }

  // AWS S3 파일 업로드 함수
  const uploadFile = async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await fileUploadClient.POST("/api/uploadFile", {
      body: formData as any,
      rawBody: true,
      credentials: "include",
      headers: {},
    });
    if (response.error) {
      console.error("파일 업로드 실패", response.error);
    }
    return response.data as string;
  };

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
    if (!isEmailVerified) {
      alert("이메일 인증을 완료해주세요.");
      return;
    }

    try {
      let profileUrl = formData.profileUrl;
      // 파일이 선택된 경우 업로드 진행 후 URL 사용
      if (selectedFile) {
        profileUrl = await uploadFile(selectedFile);
      } else {
        // 파일 선택 없을 경우 기본 이미지 사용
        profileUrl = defaultImageUrl;
      }

      const joinData = {
        ...formData,
        profileUrl,
      };

      const response = await client.POST("/api/users/signup", {
        body: joinData,
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (response.error) {
        alert(response.error.message);
        return;
      }
      alert("회원가입에 성공하였습니다.");
      router.push("/user/login");
    } catch (error: any) {
      alert("파일 업로드 중 오류가 발생했습니다: " + error.message);
    }
  }

  return (
    <div className="flex items-center justify-center min-h-screen">
      {/* 전체 폼 영역 */}
      <form onSubmit={join} className="flex flex-col gap-8 w-full max-w-4xl">
        {/* 상단 헤딩 */}
        <h2 className="text-3xl font-bold text-center">회원가입</h2>
        {/* 중간 영역: 이미지 미리보기 및 입력창 */}
        <div className="flex flex-col md:flex-row gap-8">
          {/* 왼쪽: 프로필 이미지 및 파일 선택 */}
          <div className="flex flex-col items-center">
            <img
              src={
                selectedFile
                  ? URL.createObjectURL(selectedFile)
                  : defaultImageUrl
              }
              alt="프로필 미리보기"
              className="w-48 h-48 object-cover rounded border mb-4"
            />
            <div className="relative">
              <input
                id="file-upload"
                type="file"
                className="hidden"
                accept="image/*"
                onChange={handleFileChange}
              />
              <label
                htmlFor="file-upload"
                className="cursor-pointer inline-block px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded"
              >
                프로필 사진 추가
              </label>
            </div>
          </div>
          {/* 오른쪽: 입력 폼 */}
          <div className="flex flex-col gap-4 flex-grow">
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
            <Input
              type="text"
              name="address"
              placeholder="주소"
              className="border-2 border-black"
              value={formData.address}
              onChange={handleChange}
            />
          </div>
        </div>
        {/* 하단: 회원가입 버튼 (전체 폭) */}
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
          회원가입
        </Button>
      </form>
    </div>
  );
}
