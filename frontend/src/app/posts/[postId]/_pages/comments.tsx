import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import { useState, useRef, useCallback, Component, useEffect } from "react";

interface Comment {
  id: number;
  text: string;
}

export type commentDto = components["schemas"]["CommentDto"];

interface CommentListProps {
  postId: string;
  initialComments: commentDto[];
  loadMoreComments: (page: number) => Promise<commentDto[]>;
}

export default function Comments({
  postId,
  initialComments,
  loadMoreComments,
}: CommentListProps) {
  const [comments, setComments] = useState<commentDto[]>(initialComments);
  console.log("Initial Comments:", initialComments);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const observer = useRef<IntersectionObserver | null>(null);

  const [content, setContent] = useState<string>("");

  const sendComment = async () => {
    const result = await client.POST("/api/posts/{post-id}/comments", {
      params: {
        path: {
          "post-id": postId,
        },
      },
      body: {
        content: content,
      },
      credentials: "include",
    });
    if (result.error) {
      console.log(result.error);
    }
    setComments((prev) => [result.data!.data, ...prev]);
    setContent("");
  };

  const deleteComment = async (comment: commentDto) => {
    const result = await client.DELETE(
      "/api/posts/{post-id}/comments/{comment-id}",
      {
        params: {
          path: {
            "comment-id": comment.id!,
            "post-id": postId!,
          },
        },
        credentials: "include",
      }
    );
    if (result.error) {
      console.log(result.error);
      return;
    }
    setComments((prev) => prev.filter((item) => item.id !== comment.id));
  };

  useEffect(() => {
    console.log("Initial Comments Updated:", initialComments);
    setComments(initialComments);
  }, [initialComments]);

  return (
    <div className="w-full max-h-1/2 mx-auto p-4 border-2 rounded-lg shadow-lg">
      <h2 className="text-xl font-bold mb-4">Comments</h2>
      <div className="flex gap-2">
        <Input
          value={content}
          className="border-4"
          onChange={(e) => {
            e.preventDefault();
            setContent(e.target.value);
          }}
        />
        <Button
          onClick={(e) => {
            e.preventDefault();
            sendComment();
          }}
        >
          작성하기
        </Button>{" "}
      </div>
      <div className="space-y-2">
        {comments.map((comment, index) => {
          console.log(comment);
          return (
            <div
              key={comment.id}
              className="p-2 border-b flex justify-between border-2"
            >
              {comment.content}
              <Button
                onClick={(e) => {
                  e.preventDefault();
                  deleteComment(comment);
                }}
              >
                삭제하기
              </Button>
            </div>
          );
        })}
      </div>
      {loading && (
        <p className="text-center text-gray-500">Loading more comments...</p>
      )}
    </div>
  );
}
