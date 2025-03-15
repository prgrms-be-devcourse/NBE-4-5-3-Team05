import ClientPage from "../ClientPage";
import SidebarLayout from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{
    tab: string;
  }>;
}) {
  const params = await searchParams;
  return <SidebarLayout searchParams={params}></SidebarLayout>;
}
