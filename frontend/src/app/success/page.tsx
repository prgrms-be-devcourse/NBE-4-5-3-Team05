import ClientPage from "./clientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{
    paymentType: string;
    orderId: string;
    paymentKey: string;
    amount: number;
  }>;
}) {
  const { orderId, paymentKey, amount } = await searchParams;

  return (
    <ClientPage
      orderId={orderId}
      paymentKey={paymentKey}
      amount={Number(amount)}
    ></ClientPage>
  );
}
