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
      <div className="border p-4 rounded-md w-1/2 shadow-lg">
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
          <strong>위치:</strong> { 
            loginMember.latitude === 0 && loginMember.longitude === 0 
              ? "정보 없음" 
              : `위도: ${loginMember.latitude} / 경도: ${loginMember.longitude}`
          }
        </div>
        <div className="mb-4">
          <strong>프로필 사진:</strong> <br />
          {loginMember.profileUrl ? (
            <img
              src={loginMember.profileUrl}
              alt="프로필"
              className="w-24 h-24 rounded-full mt-2"
            />
          ) : (
            <span>없음</span>
          )}
        </div>
        <Button
          onClick={() => router.push("/user/me/modify")}
          className="w-full bg-blue-500 text-white"
        >
          내 정보 수정
        </Button>
      </div>

      {/* 위도, 경도가 0이 아닐 경우 지도 표시 */}
      {(loginMember.latitude !== 0 || loginMember.longitude !== 0) && (
        <div className="mt-4 w-1/2" style={{ height: "350px" }}> {/* 지도의 높이 설정 */}
          <h1 className="text-lg mb-2">Map</h1>
          <MapComponent
            currentPos={{ lat: loginMember.latitude, lng: loginMember.longitude, zoom: 18 }}
            onLocationSelect={() => {}} // 사용자가 클릭해도 상태 변경 없도록 빈 함수
          />
        </div>
      )}

      
    </div>
  );
}
