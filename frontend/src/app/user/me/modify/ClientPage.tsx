"use client";

import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
import fileUploadClient from "@/lib/fileUploadClient";
import { useRouter } from "next/navigation";
import { useState, useContext, useEffect } from "react";
import MapComponent from "@/components/MapComponent";

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
  const [selectedProfileFile, setSelectedProfileFile] = useState<File | null>(
    null
  );

  // 위치 관리
  const [latitude, setLatitude] = useState(0);
  const [longitude, setLongitude] = useState(0);
  const [zoom, setZoom] = useState(18);
  const [locationStatus, setLocationStatus] = useState("");
  const [mapVisible, setMapVisible] = useState(false);
  const [address, setAddress] = useState("");

  useEffect(() => {
    // 초기 사용자 위치 설정 (0,0)
    setLatitude(loginMember.latitude || 0);
    setLongitude(loginMember.longitude || 0);

    const getCurrentPosition = () => {
      navigator.geolocation.getCurrentPosition((position) => {
        setLatitude(position.coords.latitude);
        setLongitude(position.coords.longitude);
      });
    };

    getCurrentPosition();
  }, [loginMember]);

  const fetchAddressFromCoordinates = async (lat: number, lng: number) => {
    if (lat === 0 || lng === 0) {
      return "주소를 입력해주세요";
    }
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`
      );
      const data = await response.json();

      if (data && data.display_name) {
        setAddress(data.display_name); // 주소를 상태에 저장
        return data.display_name;
      } else {
        setAddress("주소를 찾을 수 없습니다.");
        return "주소를 찾을 수 없습니다.";
      }
    } catch (error) {
      console.error("주소를 가져오는 데 오류 발생:", error);
      setAddress("주소를 가져오는 데 오류 발생");
      throw new Error("주소를 가져오는 데 오류 발생");
    }
  };

  const registerLocation = async (lat: number, lng: number) => {
    try {
      const response = await client.PUT("/api/users/me/location", {
        body: { latitude: lat, longitude: lng },
        credentials: "include",
      });

      if (response.error) {
        setLocationStatus("위치 등록에 실패했습니다.");
        return;
      }

      const user = response.data.data; // 서버에서 받은 사용자 정보
      setLatitude(user.latitude);
      setLongitude(user.longitude);
      setLocationStatus("위치가 성공적으로 등록되었습니다.");
      const new_address = await fetchAddressFromCoordinates(
        user.latitude,
        user.longitude
      );
      setMapVisible(false); // 위치 등록 후 지도 닫기

      await client.PUT("/api/users/me", {
        body: {
          email: loginMember.email,
          nickname: loginMember.nickname,
          address: new_address,
          profileUrl: loginMember.profileUrl,
        },
        credentials: "include",
      });
      console.log("새로얻은 주소: ", new_address);
    } catch (error) {
      console.error("위치 등록 중 오류 발생:", error);
      setLocationStatus("위치 등록에 실패했습니다.");
    }
  };

  function handleEmailChange(e: React.ChangeEvent<HTMLInputElement>) {
    const newEmail = e.target.value;
    setEmail(newEmail);

    // 이메일을 수정할 때 지도 창이 열려있다면 닫기
    if (mapVisible) {
      setMapVisible(false);
    }

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

    // 인증 코드를 입력할 때 지도 창이 열려 있다면 닫기
    if (mapVisible) {
      setMapVisible(false);
    }
  }

  // 파일 업로드 함수
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

  async function updateInfo(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();

    if (!isEmailVerified) {
      alert("이메일 인증을 완료해주세요.");
      return;
    }

    const form = e.target as HTMLFormElement;
    const emailVal = form.email.value;
    const nickname = form.nickname.value;
    const _address = address || loginMember.address;
    let profileUrl = loginMember.profileUrl;
    if (selectedProfileFile) {
      try {
        profileUrl = await uploadFile(selectedProfileFile);
      } catch (error: any) {
        alert("파일 업로드 중 오류가 발생했습니다: " + error.message);
        return;
      }
    }
    const response = await client.PUT("/api/users/me", {
      body: {
        email: emailVal,
        nickname,
        address: _address,
        profileUrl,
      },
      credentials: "include",
    });
    if (response.error) {
      alert(response.error.message);
      router.push("/user/login");
      return;
    }
    console.log("서버에 전송하는 주소: ", _address);
    alert("사용자 정보가 성공적으로 수정되었습니다.");
    setLoginMember(response.data.data);
    router.push("/user/me");
  }

  function handleProfileFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    if (e.target.files && e.target.files.length > 0) {
      setSelectedProfileFile(e.target.files[0]);
    }
  }

  return (
    <div className="flex h-full w-full justify-center p-6">
      <div className="flex flex-col gap-4 w-full max-w-3xl">
        <h2 className="text-xl font-bold text-center">회원 정보 수정</h2>
        <Button onClick={() => setMapVisible(true)} className="mt-4">
          위치 등록
        </Button>
        {mapVisible && (
          // 지도 영역을 고정된 높이로 감싸서 다른 입력 UI에 영향을 받지 않도록 함.
          <div style={{ height: "400px", width: "100%" }}>
            <MapComponent
              currentPos={{ lat: latitude, lng: longitude, zoom }}
              onLocationSelect={(lat, lng) => {
                setLatitude(lat);
                setLongitude(lng);
                registerLocation(lat, lng);
              }}
            />
          </div>
        )}
        {locationStatus && <h2>{locationStatus}</h2>}
        {latitude === 0 && longitude === 0 && (
          <h3 className="text-lg">위치를 등록해 주세요</h3>
        )}
        {latitude !== 0 && longitude !== 0 && (
          <div className="mt-4">
            <h3 className="text-lg">위치:</h3>
            <p>위도: {latitude}</p>
            <p>경도: {longitude}</p>
            {address && <p>주소: {address}</p>}
          </div>
        )}
        {/* 두 컬럼 레이아웃: 왼쪽은 프로필 사진 영역, 오른쪽은 나머지 입력 필드 */}
        <form onSubmit={updateInfo} className="flex flex-col md:flex-row gap-4">
          {/* 왼쪽 컬럼: 프로필 사진 및 사진 변경 버튼 */}
          <div className="flex flex-col items-center md:w-1/3">
            <img
              src={
                selectedProfileFile
                  ? URL.createObjectURL(selectedProfileFile)
                  : loginMember.profileUrl
              }
              alt="프로필 사진 미리보기"
              className="w-32 h-32 object-cover border rounded"
            />
            <div className="mt-2">
              <input
                id="profileFile"
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleProfileFileChange}
              />
              <label
                htmlFor="profileFile"
                className="cursor-pointer inline-block px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded"
              >
                사진 변경
              </label>
            </div>
          </div>
          {/* 오른쪽 컬럼: 나머지 입력 필드 */}
          <div className="flex flex-col gap-4 md:w-2/3">
            <div className="flex flex-col gap-1">
              <label
                htmlFor="email"
                className="block text-sm font-medium text-gray-700"
              >
                이메일:
              </label>
              <div className="flex gap-2 items-center">
                <Input
                  id="email"
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
                  className="border-2 border-black flex-grow p-2 text-base"
                  value={emailCode}
                  onChange={(e) => {
                    if (mapVisible) {
                      setMapVisible(false);
                    }
                    setEmailCode(e.target.value);
                  }}
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
                닉네임:
              </label>
              <Input
                id="nickname"
                type="text"
                name="nickname"
                placeholder="닉네임"
                className="border-2 border-black"
                defaultValue={loginMember.nickname}
                required
              />
            </div>

            <Button
              type="submit"
              className="w-full bg-blue-500 text-white py-3"
            >
              수정
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
