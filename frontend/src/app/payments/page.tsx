import RequireAuthenticated from "@/components/auth/RequireAuthenticated";
import CheckoutPage from "./ClientPage";

export default async function Page() {
  return (
    <RequireAuthenticated>
      <CheckoutPage />
    </RequireAuthenticated>
  );
}
