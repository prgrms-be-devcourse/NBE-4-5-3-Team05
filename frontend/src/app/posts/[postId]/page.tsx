"use client";

import React, { useContext, useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Image from "next/image";
import type { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import Comments from "./_pages/comments";

type ProductPostResponse = components["schemas"]["ProductPostResponse"];

export default function PostDetailPage() {
  const { postId } = useParams<{ postId: string }>();
  const router = useRouter();
  // LoginMemberContext를 useContext로 직접 읽어옵니다.
  const { loginMember } = useContext(LoginMemberContext);

  const [post, setPost] = useState<ProductPostResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [likeLoading, setLikeLoading] = useState<boolean>(false);
  const [liked, setLiked] = useState<boolean>(false);
  const [purchased, setPurchased] = useState<boolean>(false);
  const [purchaseLoading, setPurchasedLoading] = useState<boolean>(false);

  const [comments, setComments] = useState<
    components["schemas"]["CommentDto"][]
  >([]);

  const checkLoginStatus = async (): Promise<boolean> => {
    try {
      const result = await client.GET("/api/users/me", {
        credentials: "include",
      });
      if (result.error) {
        console.log("로그인 상태 확인 실패:", result.error);
        return false;
      }
      return true;
    } catch (err) {
      console.log("로그인 상태 확인 중 예외 발생:", err);
      return false;
    }
  };
  // 게시글 상세 조회 API 호출
  const fetchPost = async () => {
    if (!postId) return;
    try {
      const response = await client.GET("/api/posts/{id}", {
        withCredentials: true,
        params: { path: { id: postId } },
      });
      if (response.error) {
        console.error("게시글 상세 조회 실패", response.error);
        setError("게시글 정보를 불러올 수 없습니다.");
      } else {
        setPost(response.data.data);
      }
    } catch (err) {
      console.error("게시글 상세 조회 중 예외 발생:", err);
      setError("게시글 정보를 불러올 수 없습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 찜한 내역 조회
  const fetchUserFavorites = async () => {
    try {
      const response = await client.GET("/api/posts/my/favorites", {
        credentials: "include",
      });
      if (response.error) {
        console.error("찜한 내역 조회 실패", response.error);
        return;
      }
      const favoritesResponse = response.data.data;
      const favorites: ProductPostResponse[] = favoritesResponse.items;
      if (post?.id && favorites.some((fav) => fav.id === post.id)) {
        setLiked(true);
      }
    } catch (err) {
      console.error("찜한 내역 조회 중 예외 발생:", err);
    }
  };

  // 구매 여부 확인 (로그인 정보가 있을 때만 확인)
  const checkPurchased = async () => {
    // 로그인 정보가 없으면 바로 return
    if (!loginMember.id) return;
    try {
      const result = await client.GET("/api/payments", {
        params: { query: { "post-id": post?.id! } },
        credentials: "include",
      });
      if (result.error) {
        console.log("구매 여부 확인 실패:", result.error);
        setPurchased(false);
        return;
      }
      setPurchased(true);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchPost();
  }, [postId]);

  useEffect(() => {
    if (post) {
      fetchUserFavorites();
      checkPurchased();
      fetchComments();
    }
  }, [post]);

  const fetchComments = async () => {
    const result = await client.GET("/api/posts/{id}/comments", {
      params: {
        path: {
          id: post!.id!,
        },
        query: {
          pageable: {},
          page: 0,
          size: 10,
        },
      },
      credentials: "include",
    });
    if (result.error) {
      console.log(result.error);
      return;
    }
    setComments(result.data.data!.content!);
  };

  const loadComments = async (page: number) => {
    const response = await client.GET("/api/posts/{id}/comments", {
      params: {
        path: {
          id: post!.id!,
        },
        query: {
          pageable: {},
          page: page,
        },
      },
    });
    return response.data!.data.content!;
  };

  // 구매 처리 핸들러
  const handlePurchase = async () => {
    if (!post) return;
    const isConfirmed = confirm("정말 구매하시겠습니까?");
    if (isConfirmed) {
      setPurchasedLoading(true);
      try {
        const response = await client.POST("/api/payments", {
          body: { productId: post.id },
          credentials: "include",
        });
        if (response.error) {
          alert("구매에 실패했습니다.");
          console.error("구매 처리 실패", response.error);
          setPurchasedLoading(false);
          return;
        }
        setPurchased(true);
      } catch (err) {
        console.error(err);
      } finally {
        setPurchasedLoading(false);
      }
    }
  };

  // 찜 처리 핸들러
  const handleLike = async () => {
    if (!post || !post.id) return;
    setLikeLoading(true);
    try {
      const response = await client.POST("/api/posts/{id}/like", {
        params: { path: { id: post.id } },
        credentials: "include",
      });
      if (response.error) {
        console.error("찜 처리 실패", response.error);
        return;
      }
      setPost(response.data.data);
      setLiked(true);
    } catch (err) {
      console.error("찜 처리 중 예외 발생", err);
    } finally {
      setLikeLoading(false);
    }
  };

  // 수정하기 버튼 핸들러 (작성자와 로그인된 회원의 id 비교)
  const handleEdit = () => {
    if (!post) return;
    console.log("로그인된 회원:", loginMember);
    console.log("게시글 작성자:", post.writerId);
    if (post.writerId !== loginMember.id) {
      alert("작성자만 수정할 수 있습니다.");
      return;
    }
    router.push(`/posts/modify/${post.id}`);
  };

  // 게시글 삭제 핸들러
  const handleDelete = async () => {
    if (!post) return;
    // 작성자 확인
    if (post.writerId !== loginMember.id) {
      alert("작성자만 삭제할 수 있습니다.");
      return;
    }
    const isConfirmed = confirm("정말 게시글을 삭제하시겠습니까?");
    if (!isConfirmed) return;
    try {
      const response = await client.DELETE("/api/posts/{id}", {
        credentials: "include",
        params: { path: { id: post.id! } },
      });
      if (response.error) {
        alert("게시글 삭제에 실패했습니다: " + response.error.message);
        return;
      }
      alert("게시글이 삭제되었습니다.");
      router.push("/posts");
    } catch (err) {
      alert("삭제 중 오류가 발생했습니다.");
      console.error(err);
    }
  };

  if (loading) return <div className="p-4">로딩 중...</div>;
  if (error) return <div className="p-4 text-red-500">{error}</div>;
  if (!post) return <div className="p-4">게시글 정보를 찾을 수 없습니다.</div>;

  const images = post.imageUrls
    ? post.imageUrls
        .split(",")
        .map((url) => url.trim())
        .filter((url) => url && url.toLowerCase() !== "null")
    : [];

  return (
    <div className="p-4  w-full">
      <div className="bg-gray-800 text-white p-4 rounded mb-4">
        <h1 className="text-2xl font-bold">길게 볼 장터</h1>
      </div>
      <div className="flex flex-col md:flex-row gap-4">
        <div className="flex-1 bg-gray-100 rounded p-4">
          <h2 className="text-xl font-semibold mb-2">사진</h2>
          {images.length > 0 ? (
            <div className="relative w-full h-64">
              <Image
                src={images[0]}
                alt={post.title || "이미지"}
                fill
                className="object-cover rounded"
              />
            </div>
          ) : (
            <div className="h-64 flex items-center justify-center text-gray-500">
              이미지가 없습니다.
            </div>
          )}
        </div>
        <div className="w-full md:w-1/3 bg-blue-50 rounded p-4">
          <h2 className="text-xl font-semibold mb-2">유저 정보</h2>
          <ul className="space-y-1">
            <li>작성자 ID: {post.writerId}</li>
            <li>작성자 닉네임: {post.writerName}</li>
          </ul>
          <div className="mt-4">
            <button
              onClick={() => router.push(`/chat/${post.id}`)}
              className="px-4 py-2 bg-blue-500 text-white rounded"
            >
              채팅 걸기
            </button>
          </div>
        </div>
      </div>
      <div className="mt-4 bg-white rounded p-4 shadow">
        <div className="flex flex-col gap-2 md:flex-row md:justify-between md:items-center">
          <h2 className="text-2xl font-bold">{post.title}</h2>
          <div className="text-gray-500 text-sm">
            <span className="mr-2">
              작성일:{" "}
              {post.createdAt
                ? new Date(post.createdAt).toLocaleDateString()
                : ""}
            </span>
            <span className="mr-2">조회수: {post.viewCount}</span>
            <span className="mr-2">찜: {post.likedCount}</span>
          </div>
        </div>
        <hr className="my-2" />
        <div className="flex flex-col gap-2 md:flex-row md:gap-8">
          <div>
            <strong>카테고리:</strong>{" "}
            {post.categories && post.categories.length > 0
              ? post.categories.join(", ")
              : "없음"}
          </div>
          <div>
            <strong>가격:</strong> {post.productPrice}원
          </div>
          <div>
            <strong>위치:</strong> 위도 {post.latitude}, 경도 {post.longitude}
          </div>
        </div>
        <hr className="my-2" />
        <div>
          <h3 className="text-lg font-semibold mb-2">본문 내용</h3>
          <p>{post.content}</p>
        </div>
        {images.length > 1 && (
          <div className="mt-4">
            <h3 className="text-lg font-semibold mb-2">추가 사진</h3>
            <div className="flex gap-2 overflow-x-auto">
              {images.slice(1).map((imgUrl, idx) => (
                <div key={idx} className="relative w-40 h-40">
                  <Image
                    src={imgUrl}
                    alt={`${post.title} - ${idx + 1}`}
                    fill
                    className="object-cover rounded"
                  />
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
      <div className="mt-4 flex gap-4">
        <button
          disabled={likeLoading || liked}
          onClick={handleLike}
          className="px-4 py-2 bg-red-500 text-white rounded"
        >
          {liked ? "찜 완료" : likeLoading ? "처리 중..." : "찜하기"}
        </button>
        <button
          disabled={purchaseLoading || purchased}
          onClick={handlePurchase}
          className="px-4 py-2 bg-red-500 text-white rounded"
        >
          {purchased
            ? "구매 완료"
            : purchaseLoading
            ? "처리 중..."
            : "구매하기"}
        </button>
        <button
          onClick={handleEdit}
          className="px-4 py-2 bg-blue-500 text-white rounded"
        >
          수정하기
        </button>
        <button
          onClick={handleDelete}
          className="px-4 py-2 bg-gray-500 text-white rounded"
        >
          삭제하기
        </button>
        <div className="w-full">
          <Comments
            postId={post.id!}
            initialComments={comments}
            loadMoreComments={loadComments}
          ></Comments>
        </div>
      </div>
    </div>
  );
}
