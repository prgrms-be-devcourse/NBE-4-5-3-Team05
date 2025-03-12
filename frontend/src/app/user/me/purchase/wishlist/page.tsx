import client from "@/lib/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";
import { parseAccessToken } from "@/app/util/auth";
import { redirect } from "next/navigation";

export default async function Page() {
  const myCookie = await cookies();

  const { isLogin, payload } = parseAccessToken(myCookie.get("accessToken"));

  if (!isLogin) {
    redirect("/");
  }

  return <ClientPage />;
}
