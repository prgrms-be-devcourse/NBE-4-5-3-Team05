import ClientPage from "./ClientPage";

export default function Page() {
  // 로그인 상태가 없으면 기본값 전달
  const me = {
    id: "",
    username: "",
    email: "",
    nickname: "",
    address: "",
    profileUrl: "",
    role: "USER",
    createdAt: "",
    modifiedAt: "",
    blocked: false,
    blockedCount: 0,
  };

  return <ClientPage me={me} />;
}
