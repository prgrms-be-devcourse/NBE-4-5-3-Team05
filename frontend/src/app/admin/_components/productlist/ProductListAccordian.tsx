import { ProductListItem } from "@/app/_type/ProductListItem";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@radix-ui/react-accordion";
import { ChevronDown, Trash2 } from "lucide-react";

export default function ShadcnAccordionList({
  items,
}: {
  items: ProductListItem[];
}) {
  const doDelete = async (item: ProductListItem) => {
    const response = await client.DELETE("/api/admin/posts/{post-id}", {
      params: {
        path: {
          "post-id": item.id!,
        },
      },
      credentials: "include",
    });
    if (response.error) {
      console.log(response.response);
    }
    window.location.reload();
  };
  return (
    <div className="w-full mx-auto overflow-y-auto border rounded-lg px-2">
      <Accordion type="multiple" className="w-full mx-auto  ">
        {items.map((item) => (
          <AccordionItem
            key={item.id}
            value={String(item.id)}
            className="border rounded-lg border-gray-300 my-2 "
          >
            <AccordionTrigger className="w-full h-full flex justify-between px-3 text-xl hover:cursor-pointer py-4">
              <p>{item.id}</p>
              <ChevronDown className="ml-2" size={20} />
            </AccordionTrigger>

            <AccordionContent className="flex justify-between p-6 min-h-48 bg-gray-300 ">
              <div className="flex-col justify-between">
                <p>
                  작성자 : {item.writerName} ({item.writerId})
                </p>
                <p>판매글 id : {item.id}</p>
                <p>판매글 제목 : {item.title}</p>
                <p>판매 상품 이름 : {item.productName}</p>
                <p>판매 상품 가격 : {item.productPrice}</p>
                <p>글 작성 시간 : {item.createdAt}</p>
                <p>{item.thumbNail}</p>
              </div>

              <button
                className="p-3 text-red-500 hover:text-red-700"
                onClick={(e) => {
                  e.preventDefault();
                  const userConfirmed =
                    window.confirm("정말로 삭제하시겠습니까?");
                  if (userConfirmed) {
                    doDelete(item);
                    // 여기에 실행할 동작을 추가하세요
                  } else {
                    console.log("사용자가 취소를 클릭했습니다.");
                  }
                }}
              >
                <Trash2 size={40} />
              </button>
            </AccordionContent>
          </AccordionItem>
        ))}
      </Accordion>
    </div>
  );
}
