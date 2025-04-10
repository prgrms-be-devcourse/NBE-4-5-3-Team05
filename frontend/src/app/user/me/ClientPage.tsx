"use client";

import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import MapComponent from "@/components/MapComponent";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { use } from "react";

export default function ClientPage() {
  const router = useRouter();
  const { loginMember } = use(LoginMemberContext);

  return (
    <div className="flex flex-col items-center p-6 w-full">
      <h2 className="text-2xl font-bold mb-4">내 정보 조회</h2>

      {/* 회원 정보 영역의 최대 너비를 늘려 넉넉하게 표시 */}
      <div className="flex flex-col md:flex-row border p-6 rounded-md w-full max-w-5xl shadow-lg">
        {/* 왼쪽: 프로필 사진 (정사각형, 크게) */}
        <div className="flex-shrink-0 flex items-center justify-center mb-4 md:mb-0 md:mr-6">
          {loginMember.profileUrl ? (
            <img
              src={loginMember.profileUrl}
              alt="프로필"
              className="w-80 h-80 object-cover border"
            />
          ) : (
            <div className="w-80 h-80 flex items-center justify-center border bg-gray-100">
              <span>없음</span>
            </div>
          )}
        </div>

        {/* 오른쪽: 회원 정보 영역 (flex-grow 추가로 공간 활용) */}
        <div className="flex flex-col justify-center flex-grow">
          <div className="mb-2">
            <strong>아이디:</strong> {loginMember.username}
          </div>
          <div className="mb-2">
            <strong>이메일:</strong> {loginMember.email}
          </div>
          <div className="mb-2">
            <strong>닉네임:</strong> {loginMember.nickname}
          </div>
          <div className="mb-2">
            <strong>주소:</strong> {loginMember.address || "주소 없음"}
          </div>
          <div className="mb-2">
            <strong>위치:</strong>{" "}
            {loginMember.latitude === 0 && loginMember.longitude === 0
              ? "정보 없음"
              : `위도: ${loginMember.latitude} / 경도: ${loginMember.longitude}`}
          </div>
          <Button
            onClick={() => router.push("/user/me/modify")}
            className="w-full md:w-auto bg-blue-500 text-white mt-4"
          >
            내 정보 수정
          </Button>
        </div>
      </div>

      {/* 지도 영역 */}
      {(loginMember.latitude !== 0 || loginMember.longitude !== 0) && (
        <div className="mt-4 w-full max-w-5xl" style={{ height: "350px" }}>
          <h1 className="text-lg mb-2">Map</h1>
          <MapComponent
            currentPos={{
              lat: loginMember.latitude,
              lng: loginMember.longitude,
              zoom: 18,
            }}
            onLocationSelect={() => {}}
          />
        </div>
      )}
    </div>
  );
}
