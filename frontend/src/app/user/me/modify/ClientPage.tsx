"use client";

import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
import { useRouter } from "next/navigation";
import { useState, useContext } from "react";

export default function ClientPage() {
  const router = useRouter();
  const { loginMember, setLoginMember } = useContext(LoginMemberContext);

  // 이메일 인증 상태 관리
  const [email, setEmail] = useState(loginMember.email || "");
  const [isEmailSent, setIsEmailSent] = useState(false);
  const [emailCode, setEmailCode] = useState("");
  const [isEmailVerified, setIsEmailVerified] = useState(true); // 기존 이메일은 인증된 상태
  const [isLoading, setIsLoading] = useState(false);
  const [emailStatus, setEmailStatus] = useState("");
  const [emailStatusColor, setEmailStatusColor] = useState("text-red-500");
  const [codeStatus, setCodeStatus] = useState("");
  const [codeStatusColor, setCodeStatusColor] = useState("text-red-500");

  function handleEmailChange(e: React.ChangeEvent<HTMLInputElement>) {
    const newEmail = e.target.value;
    setEmail(newEmail);

    if (newEmail === loginMember.email) {
      // 기존 이메일로 돌아가면 인증 필요 없음
      setIsEmailVerified(true);
      setIsEmailSent(false);
      setEmailStatus("");
    } else {
      // 이메일이 변경되면 다시 인증 필요
      setIsEmailVerified(false);
      setIsEmailSent(false);
      setEmailStatus("");
    }
  }

  async function sendEmailVerification() {
    if (!email.trim()) {
      alert("이메일을 입력해주세요.");
      return;
    }

    setIsLoading(true);
    setEmailStatus("인증 코드 발송 중입니다. 잠시만 기다려 주세요");
    setEmailStatusColor("text-red-500");
    setIsEmailSent(true);

    const response = await client.POST("/api/users/email/code", {
      body: { email },
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
      body: { email, code: emailCode },
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

  async function updateInfo(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();

    if (!isEmailVerified) {
      alert("이메일 인증을 완료해주세요.");
      return;
    }

    const formData = e.target as HTMLFormElement;
    console.log(formData);
    const email = formData.email.value;
    const nickname = formData.nickname.value;
    const address = formData.address.value;
    const profileUrl = formData.profileUrl.value;

    const response = await client.PUT("/api/users/me", {
      body: {
        email,
        nickname,
        address,
        profileUrl,
      },
      credentials: "include",
    });

    if (response.error) {
      alert(response.error.message);
      router.push("/user/login");
      return;
    }

    alert("사용자 정보가 성공적으로 수정되었습니다.");
    setLoginMember(response.data.data);
    router.push("/user/me");
  }

  return (
    <>
      <div className="flex h-full w-full justify-center">
        <div className="flex flex-col gap-2">
          <h2 className="text-xl font-bold text-center">회원 정보 수정</h2>
          <form onSubmit={updateInfo} className="flex flex-col gap-2">
            <div className="flex flex-col gap-1">
              <label
                htmlFor="email"
                className="block text-sm font-medium text-gray-700"
              >
                {" "}
                이메일:
              </label>
              <div className="flex gap-2 items-center">
                <Input
                  type="email"
                  name="email"
                  placeholder="이메일"
                  className="border-2 border-black flex-grow"
                  value={email}
                  onChange={handleEmailChange}
                  required
                />
                <Button
                  type="button"
                  className={`p-2 ${
                    isLoading || email === loginMember.email || !email.trim()
                      ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                      : "bg-blue-500 text-white"
                  }`}
                  onClick={sendEmailVerification}
                  disabled={email === loginMember.email || !email.trim()}
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
              <div className="flex gap-2">
                <Input
                  type="text"
                  placeholder="인증 코드"
                  className="border-2 border-black flex-grow"
                  value={emailCode}
                  onChange={(e) => setEmailCode(e.target.value)}
                />
                <Button
                  type="button"
                  className="bg-green-500 text-white p-2"
                  onClick={verifyEmailCode}
                >
                  확인
                </Button>
              </div>
            )}
            {codeStatus && (
              <p className={`text-sm ${codeStatusColor}`}>{codeStatus}</p>
            )}

            <div>
              <label
                htmlFor="nickname"
                className="block text-sm font-medium text-gray-700"
              >
                {" "}
                닉네임:
              </label>
              <Input
                type="text"
                name="nickname"
                placeholder="닉네임"
                className="border-2 border-black"
                defaultValue={loginMember.nickname}
              />
            </div>
            <div>
              <label
                htmlFor="address"
                className="block text-sm font-medium text-gray-700"
              >
                주소:
              </label>
              <Input
                type="text"
                name="address"
                placeholder="주소"
                className="border-2 border-black w-[500px]"
                defaultValue={loginMember.address}
              />
            </div>
            <div>
              <label
                htmlFor="profileUrl"
                className="block text-sm font-medium text-gray-700"
              >
                프로필URL:
              </label>
              <Input
                type="url"
                name="profileUrl"
                placeholder="프로필URL"
                className="border-2 border-black w-[500px]"
                defaultValue={loginMember.profileUrl}
              />
            </div>

            <Button
              type="submit"
              className={`p-2 mt-4 ${
                isEmailVerified
                  ? "bg-blue-500 text-white"
                  : "bg-gray-300 text-gray-500 cursor-not-allowed"
              }`}
              disabled={!isEmailVerified}
            >
              수정
            </Button>
          </form>
        </div>
      </div>
    </>
  );
}
