import RequireAdmin from "@/components/auth/RequireAdmin";
import SidebarLayout from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{
    tab: string;
  }>;
}) {
  const params = await searchParams;
  return (
    <RequireAdmin>
      <SidebarLayout searchParams={params}></SidebarLayout>
    </RequireAdmin>
  );
}
