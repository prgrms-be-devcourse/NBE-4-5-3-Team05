import { useState, useRef, useCallback } from "react";

interface Comment {
  id: number;
  text: string;
}

interface CommentListProps {
  initialComments: Comment[];
  loadMoreComments: (page: number) => Promise<Comment[]>;
}

export default function Comments({
  initialComments,
  loadMoreComments,
}: CommentListProps) {
  const [comments, setComments] = useState<Comment[]>(initialComments);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const observer = useRef<IntersectionObserver | null>(null);

  const lastCommentRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (loading) return;
      if (observer.current) observer.current.disconnect();
      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
          setLoading(true);
          loadMoreComments(page)
            .then((newComments) => {
              setComments((prev) => [...prev, ...newComments]);
              setPage((prev) => prev + 1);
            })
            .finally(() => setLoading(false));
        }
      });
      if (node) observer.current.observe(node);
    },
    [loading, page, loadMoreComments]
  );

  return (
    <div className="w-full max-w-lg mx-auto p-4 border rounded-lg shadow-lg">
      <h2 className="text-xl font-bold mb-4">Comments</h2>
      <div className="space-y-2">
        {comments.map((comment, index) => (
          <div
            key={comment.id}
            ref={index === comments.length - 1 ? lastCommentRef : null}
            className="p-2 border-b"
          >
            {comment.text}
          </div>
        ))}
      </div>
      {loading && (
        <p className="text-center text-gray-500">Loading more comments...</p>
      )}
    </div>
  );
}
