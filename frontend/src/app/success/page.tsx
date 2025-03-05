import { useRouter } from "next/navigation";
import ClientPage from "./clientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: {
    paymentType: string;
    orderId: string;
    paymentKey: string;
    amount: number;
  };
}) {
  const { paymentType, orderId, paymentKey, amount } = await searchParams;



  return <ClientPage orderId={orderId} paymentKey={paymentKey} amount={amount}></ClientPage>;
}
