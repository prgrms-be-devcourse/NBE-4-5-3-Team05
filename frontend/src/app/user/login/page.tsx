import RequireAnonymous from "@/components/auth/RequireAnonymous";
import ClientPage from "./ClientPage";

export default async function Page() {
  return (
    <RequireAnonymous>
      <ClientPage />
    </RequireAnonymous>
  );
}
