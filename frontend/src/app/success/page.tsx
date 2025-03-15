import { useRouter } from "next/navigation";
import ClientPage from "./clientPage";

type searchParamType = {
  paymentType: string;
  orderId: string;
  paymentKey: string;
  amount: string;
};

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<searchParamType>;
}) {
  const { paymentType, orderId, paymentKey, amount } = await searchParams;

  return (
    <ClientPage
      orderId={orderId}
      paymentKey={paymentKey}
      amount={Number(amount)}
    ></ClientPage>
  );
}
