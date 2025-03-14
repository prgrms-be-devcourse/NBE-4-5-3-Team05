"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { useEffect, useState } from "react";
import client from "@/lib/client";
import { NoticeListItem } from "@/app/_type/NoticeListItem";

const formSchema = z.object({
  title: z.string().min(3, "제목은 최소 3자 이상 입력해야 합니다."),
  content: z.string().min(10, "내용은 최소 10자 이상 입력해야 합니다."),
});

type FormData = z.infer<typeof formSchema>;

const TitleContentForm = ({ editNotice }: { editNotice?: NoticeListItem }) => {
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(formSchema),
  });

  useEffect(() => {
    if (editNotice) {
      setValue("title", editNotice.title!);
      setValue("content", editNotice.content!);
    }
  }, [editNotice, setValue]);

  const submitNotice = async (data: FormData) => {
    if (editNotice) {
      // 수정 진행
      const response = await client.PUT("/api/admin/notices/{notice-id}", {
        params: {
          path: {
            "notice-id": editNotice.id!,
          },
        },
        body: {
          title: data.title,
          content: data.content,
        },
        credentials: "include",
      });
      if (response.error) {
        console.log(response.error);
        return;
      }
      alert("공지사항 수정 성공.");
      window.location.reload();
    } else {
      const response = await client.POST("/api/admin/notices", {
        body: {
          title: data!.title,
          content: data!.content,
        },
        credentials: "include",
      });
      if (response.error) {
        console.log(response.error);
        return;
      }
      alert("공지사항 작성 성공.");
      window.location.reload();
    }
  };

  const onSubmit = (data: FormData) => {
    submitNotice(data);
  };

  return (
    <Card className="flex-1 h-full  p-5 shadow-md">
      <CardHeader>
        <h2 className="text-lg font-semibold">
          {editNotice ? "공지사항 수정" : "공지사항 작성"}
        </h2>
      </CardHeader>
      <CardContent className="h-full flex flex-col flex-1">
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="space-y-4 flex flex-col flex-1"
        >
          {/* 제목 입력 */}
          <div>
            <Label htmlFor="title" className="text-2xl font-bold">
              제목
            </Label>
            <Input
              id="title"
              className="text-3xl  h-12 w-full px-4 py-3 border rounded-lg"
              placeholder="제목을 입력하세요"
              {...register("title")}
            />
            {errors.title && (
              <p className="text-red-500 text-sm">{errors.title.message}</p>
            )}
          </div>

          {/* 내용 입력 */}
          <div className="flex-1 flex flex-col">
            <Label htmlFor="content" className="text-2xl font-bold">
              내용
            </Label>
            <Textarea
              id="content"
              placeholder="내용을 입력하세요"
              className={
                "text-7xl w-full px-4 py-3 border rounded-lg flex-1 resize-none"
              }
              {...register("content")}
            />
            {errors.content && (
              <p className="text-red-500 text-sm">{errors.content.message}</p>
            )}
          </div>

          {/* 제출 버튼 */}
          <Button type="submit" className="w-full">
            {editNotice ? "수정하기" : "작성하기"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
};

export default TitleContentForm;
