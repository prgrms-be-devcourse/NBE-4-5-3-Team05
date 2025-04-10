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
  loginMember: components["schemas"]["UserDto"];
}

export default function Comments({
  postId,
  initialComments,
  loadMoreComments,
  loginMember,
}: CommentListProps) {
  const [comments, setComments] = useState<commentDto[]>(initialComments);
  console.log("Initial Comments:", initialComments);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const observer = useRef<IntersectionObserver | null>(null);

  const [content, setContent] = useState<string>("");

  const [editingCommentId, setEditingCommentId] = useState<string | null>(null);
  const [editContent, setEditContent] = useState<string>("");

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

  const updateComment = async (commentId: string) => {
    const result = await client.PUT(
      "/api/posts/{post-id}/comments/{comment-id}",
      {
        params: {
          path: {
            "post-id": postId,
            "comment-id": commentId,
          },
        },
        body: {
          content: editContent,
        },
        credentials: "include",
      }
    );

    if (result.error) {
      console.log(result.error);
      return;
    }

    // 댓글 목록 갱신
    setComments((prev) =>
      prev.map((item) =>
        item.id === commentId
          ? { ...item, content: result.data!.data.content }
          : item
      )
    );
    setEditingCommentId(null);
    setEditContent("");
  };

  const deleteComment = async (comment: commentDto) => {
    const isConfirmed = confirm("정말 이 댓글을 삭제하시겠습니까?");
    if (!isConfirmed) return;

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
    alert("댓글이 성공적으로 삭제되었습니다.");
  };

  useEffect(() => {
    console.log("Initial Comments Updated:", initialComments);
    setComments(initialComments);
  }, [initialComments]);

  return (
    <div className="w-full max-h-1/2 mx-auto p-4 border-2 rounded-lg shadow-lg">
      <h2 className="text-xl font-bold mb-4">댓글</h2>

      {/* 댓글 작성 */}
      <div className="flex gap-2 mb-4">
        <Input
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="댓글을 입력하세요"
          className="border-2"
        />
        <Button onClick={sendComment}>작성하기</Button>
      </div>

      {/* 댓글 목록 */}
      <div className="space-y-2">
        {comments.map((comment) => {
          const isEditing = editingCommentId === comment.id;
          const isAuthor = loginMember.id === comment.author.id;

          return (
            <div
              key={comment.id}
              className="p-2 border-b flex justify-between items-start border-2"
            >
              <div className="flex flex-col w-full">
                <div className="text-sm font-semibold mb-1">
                  {comment.author.nickname}
                </div>

                {isEditing ? (
                  <div className="flex gap-2 items-center">
                    <Input
                      value={editContent}
                      onChange={(e) => setEditContent(e.target.value)}
                    />
                    <Button onClick={() => updateComment(comment.id!)}>
                      저장
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => {
                        setEditingCommentId(null);
                        setEditContent("");
                      }}
                    >
                      취소
                    </Button>
                  </div>
                ) : (
                  <div className="flex justify-between items-center">
                    <p>{comment.content}</p>
                    {isAuthor && (
                      <div className="flex gap-2 ml-4">
                        <Button
                          onClick={() => {
                            setEditingCommentId(comment.id!);
                            setEditContent(comment.content);
                          }}
                        >
                          수정
                        </Button>
                        <Button onClick={() => deleteComment(comment)}>
                          삭제
                        </Button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
