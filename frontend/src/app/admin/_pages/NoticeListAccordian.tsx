import { NoticeListItem } from "@/app/_type/NoticeListItem";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@radix-ui/react-accordion";
import { ChevronDown } from "lucide-react";

export default function NoticeListAccordian({
  items,
  onEdit,
  onDelete,
}: {
  items: NoticeListItem[];
  onEdit: (notice: NoticeListItem) => void;
  onDelete: (notice: NoticeListItem) => void;
}) {
  return (
    <div className="flex-1  overflow-y-auto border rounded-lg px-2">
      <Accordion type="multiple" className="w-full mx-auto mt-5 ">
        {items.map((item) => (
          <AccordionItem
            key={item.id}
            value={String(item.id)}
            className="border rounded-lg border-gray-300 my-2 "
          >
            <AccordionTrigger className="w-full h-full flex justify-between px-3 text-xl hover:cursor-pointer py-4">
              <p>{item.title}</p>
              <div className="flex">
                <p className="opacity-40">{item.createdAt}</p>
                <ChevronDown className="ml-2" size={20} />
              </div>
            </AccordionTrigger>

            <AccordionContent className="flex justify-between p-6 min-h-48 bg-gray-300 ">
              <div className="flex-col justify-between">
                <p>id : {item.id}</p>
                <p>{item.content}</p>
                <p>작성자 : {item.admin?.nickname}</p>
              </div>

              <div className="flex gap-2">
                <button
                  className="px-3 py-1 bg-yellow-500 text-white rounded"
                  onClick={() => onEdit(item)}
                >
                  수정
                </button>
                <button
                  className="px-3 py-1 bg-red-500 text-white rounded"
                  onClick={(e) => {
                    e.preventDefault();
                    onDelete(item);
                  }}
                >
                  삭제
                </button>
              </div>
            </AccordionContent>
          </AccordionItem>
        ))}
      </Accordion>
    </div>
  );
}
