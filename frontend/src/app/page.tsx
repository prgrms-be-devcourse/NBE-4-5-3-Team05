"use client";

export default function Page() {
  return (
    <div>
      <button>
        <a href="http://localhost:8080/oauth2/authorization/kakao?redirectUrl=localhost:3000">
          카카오 로그인
        </a>
      </button>
    </div>
  );
}
