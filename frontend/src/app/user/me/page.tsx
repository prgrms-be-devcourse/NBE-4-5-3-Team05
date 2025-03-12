import client from "@/lib/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export default async function Page() {
  const response = await client.GET("/api/users/me", {
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  const rsData = response.data;

  if (!rsData) {
    redirect("/");
  }

  const userInfo = rsData.data;

  return <ClientPage userInfo={userInfo} />;
}
