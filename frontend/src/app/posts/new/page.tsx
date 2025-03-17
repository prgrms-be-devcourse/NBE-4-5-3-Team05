import RequireAuthenticated from "@/components/auth/RequireAuthenticated";
import ClientPage from "./ClientPage";

export default function page() {
  return (
    <RequireAuthenticated>
      <ClientPage></ClientPage>
    </RequireAuthenticated>
  );
}
