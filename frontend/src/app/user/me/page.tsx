import ClientPage from "./ClientPage";
import RequireAuthenticated from "@/components/auth/RequireAuthenticated";

export default async function Page() {
  return (
    <RequireAuthenticated>
      <ClientPage />
    </RequireAuthenticated>
  );
}
