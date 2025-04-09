"use client";

import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import client from "@/lib/client";
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

  const fetchAddressFromCoordinates = async (lat:any, lng:any) => {
    if(lat==0 || lng==0){
      return "주소를 입력해주세요" ;
    }
    try {
      const response = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`);
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

  const registerLocation = async (lat:any, lng:any) => {
    // if (lat === 0 && lng === 0) {
    //     alert("위치 정보를 가져오지 못했습니다.");
    //     return;
    // }

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
      setLatitude(user.latitude);  // 서버에서 받은 위도
      setLongitude(user.longitude); // 서버에서 받은 경도
      setLocationStatus("위치가 성공적으로 등록되었습니다.");
      const new_address = await fetchAddressFromCoordinates(user.latitude, user.longitude); // 주소 패치
      setMapVisible(false); // 위치 등록 후 지도 닫기

      const updateUserResponse = await client.PUT("/api/users/me", {
        body: {
          emial: loginMember.email,
          nickname: loginMember.nickname,
          address: new_address, // 위도 경도로 얻은 새 주소
          profileUrl: loginMember.profileUrl,
        },
        credentials: "include",
      });
      console.log("새로얻은 주소: ",new_address);

    } catch (error) {
        console.error("위치 등록 중 오류 발생:", error);
        setLocationStatus("위치 등록에 실패했습니다.");
    }
  };


  
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
    const _address = address || loginMember.address;
    const profileUrl = formData.profileUrl.value;

    const response = await client.PUT("/api/users/me", {
      body: {
        email,
        nickname,
        address:_address,
        profileUrl,
      },
      credentials: "include",
    });

    if (response.error) {
      alert(response.error.message);
      router.push("/user/login");
      return;
    }

    console.log("서버에 전송하는 주소: ",_address);
    alert("사용자 정보가 성공적으로 수정되었습니다.");
    setLoginMember(response.data.data);
    router.push("/user/me");
  }

  return (
    <>
      <div className="flex h-full w-full justify-center">
        <div className="flex flex-col gap-2">
          <h2 className="text-xl font-bold text-center">회원 정보 수정</h2>

          <Button onClick={() => {
              setMapVisible(true); // 지도를 표시
          }} className="mt-4">
              위치 등록
          </Button>


          {mapVisible && (
              <MapComponent
                  currentPos={{ lat: latitude, lng: longitude, zoom }}
                  onLocationSelect={(lat, lng) => {
                      setLatitude(lat);
                      setLongitude(lng);
                      registerLocation(lat, lng); // 핀 클릭 시 위치 등록
                  }}
              />
          )}

          {locationStatus && <h2>{locationStatus}</h2>}
          
          {latitude == 0 && longitude == 0 && (
            <h3 className="text-lg">위치를 등록해 주세요</h3>
          )}
            
          {latitude !== 0 && longitude !== 0 && (
            <div className="mt-4">
              <h3 className="text-lg">위치:</h3>
              <p>위도: {latitude}</p>
              <p>경도: {longitude}</p>
              {address && <p>주소: {address}</p>} {/* 주소 표시 */}
            </div>
          )}


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
            {/* <div>
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
            </div> */}
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
