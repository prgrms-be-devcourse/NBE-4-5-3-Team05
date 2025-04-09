"use client";

import React, { useContext, useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Image from "next/image";
import type { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/client";
import { LoginMemberContext } from "@/app/stores/auth/loginMemberStore";
import Comments from "./_pages/comments";
import { Button } from "@/components/ui/button";
import { faComment, faMapMarkerAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import TradeLocationModal from "@/components/TradeLocationModal"; // 경로는 실제 파일 위치에 맞게 조정

type ProductPostResponse = components["schemas"]["ProductPostResponse"];
type PreviewPostResponse = components["schemas"]["PreviewPostResponse"];
type StatusType = "AVAILABLE" | "RESERVED" | "PURCHASED";

export default function PostDetailPage() {
  const { postId } = useParams<{ postId: string }>();
  const router = useRouter();
  const { isLogin, loginMember } = useContext(LoginMemberContext);

  const [post, setPost] = useState<ProductPostResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [likeLoading, setLikeLoading] = useState<boolean>(false);
  const [liked, setLiked] = useState<boolean>(false);
  const [purchased, setPurchased] = useState<boolean>(false);
  const [purchaseLoading, setPurchasedLoading] = useState<boolean>(false);
  const [statusLoading, setStatusLoading] = useState<boolean>(false);
  const [comments, setComments] = useState<
    components["schemas"]["CommentDto"][]
  >([]);

  // 거래위치 모달 열림 상태
  const [tradeModalOpen, setTradeModalOpen] = useState<boolean>(false);

  const statusLabels: Record<StatusType, string> = {
    AVAILABLE: "판매중",
    RESERVED: "예약중",
    PURCHASED: "판매완료",
  };

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
        params: { path: { id: postId } },
        credentials: "include",
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
      const favorites: PreviewPostResponse[] = favoritesResponse.items;
      if (post?.id && favorites.some((fav) => fav.id === post.id)) {
        setLiked(true);
      }
    } catch (err) {
      console.error("찜한 내역 조회 중 예외 발생:", err);
    }
  };

  // 상품 상태 변경 핸들러
  const handleStatusChange = async (newStatus: StatusType) => {
    if (!post) return;
    if (post.writerId !== loginMember.id) {
      alert("작성자만 상태를 변경할 수 있습니다.");
      return;
    }

    const confirmChange = confirm(
      `상품 상태를 "${statusLabels[newStatus]}"로 변경하시겠습니까?`
    );
    if (!confirmChange) return;

    setStatusLoading(true);
    try {
      const response = await client.PUT("/api/posts/{id}", {
        params: { path: { id: post.id! } },
        body: { status: newStatus },
        credentials: "include",
      });

      if (response.error) {
        alert("상품 상태 변경 실패: " + response.error.message);
        return;
      }

      // 상태 변경 성공 시 즉시 UI 업데이트
      setPost((prev) => (prev ? { ...prev, status: newStatus } : prev));
      alert(`상품 상태가 "${statusLabels[newStatus]}"로 변경되었습니다.`);
    } catch (err) {
      console.error("상품 상태 변경 중 오류 발생:", err);
      alert("상품 상태 변경 중 오류가 발생했습니다.");
    } finally {
      setStatusLoading(false);
    }
  };

  // 구매 여부 확인 (로그인 정보가 있을 때만 확인)
  const checkPurchased = async () => {
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
      setPurchased(result.data.data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchPost();
  }, [postId]);

  useEffect(() => {
    if (post) {
      if (isLogin) {
        fetchUserFavorites();
        checkPurchased();
      }
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

  // 수정하기 버튼 핸들러
  const handleEdit = () => {
    if (!post) return;
    if (post.writerId !== loginMember.id) {
      alert("작성자만 수정할 수 있습니다.");
      return;
    }
    router.push(`/posts/modify/${post.id}`);
  };

  // 게시글 삭제 핸들러
  const handleDelete = async () => {
    if (!post) return;
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

  // 이미지 URL 처리 (소문자, trim 적용)
  const images = post.imageUrls
    ? post.imageUrls
        .split(",")
        .map((url) => url.trim())
        .filter((url) => url && url.toLowerCase() !== "null")
    : [];

  // 거래 위치 정보 : post.location 이 있다면 사용하고, 없을 경우 "등록된 주소 없음"으로 처리
  const tradeLocation = (post as any).location || "등록된 주소 없음";

  return (
    <div className="p-4 w-full">
      <div className="bg-gray-800 text-white p-4 rounded mb-4">
        <h1 className="text-2xl font-bold">길게 볼 장터</h1>
      </div>
      <div className="flex flex-col md:flex-row gap-4">
        <div className="flex-1 bg-gray-100 rounded p-4">
          <h2 className="text-xl font-semibold mb-2">사진</h2>
          {images.length > 0 ? (
            <div className="relative w-full h-64">
              <Image
                loader={() => images[0]}
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
            <li>작성자 닉네임: {post.writerName}</li>
          </ul>
          <div className="mt-4 flex flex-col gap-2">
            {isLogin && (
              <>
                <Button
                  variant="outline"
                  onClick={async () => {
                    // 채팅방 생성 처리
                    const isLoggedIn = await checkLoginStatus();
                    if (!isLoggedIn) {
                      alert("먼저 로그인을 해주세요.");
                      router.push("/user/login");
                      return;
                    }
                    try {
                      const createResponse = await client.POST(
                        "/api/chat/room",
                        {
                          params: {
                            query: {
                              postId: postId,
                            },
                          },
                          credentials: "include",
                        }
                      );
                      if (createResponse.error) {
                        console.error(
                          "채팅방 생성 오류:",
                          createResponse.error.message
                        );
                        alert("채팅방 생성에 실패했습니다.");
                        return;
                      }
                      const chatRoomId = createResponse.data.data.roomId;
                      router.push(`/chat/${chatRoomId}`);
                    } catch (error) {
                      console.error("채팅방 생성 중 오류 발생:", error);
                      alert("채팅방 생성 중 오류가 발생했습니다.");
                    }
                  }}
                  className="rounded-full bg-yellow-400 text-black py-2 px-4 border border-black-700 hover:bg-yellow-300"
                >
                  <FontAwesomeIcon icon={faComment} className="mr-2" />
                  채팅
                </Button>
                <Button
                  variant="outline"
                  onClick={() => setTradeModalOpen(true)}
                  className="rounded-full bg-blue-200 text-blue-900 py-2 px-4 hover:bg-blue-300"
                >
                  <FontAwesomeIcon icon={faMapMarkerAlt} className="mr-2" />
                  거래위치 보기
                </Button>
              </>
            )}
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
            <strong>거래 위치:</strong> {tradeLocation}
          </div>
          <div>
            <strong>위도:</strong> {post.latitude}
          </div>
          <div>
            <strong>경도:</strong> {post.longitude}
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
                    loader={() => imgUrl}
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

      {/* 상태 변경 & 액션 버튼 영역 */}
      <div className="bg-gray-50 p-6 rounded-lg shadow mb-10">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
          {post.writerId === loginMember.id && (
            <div className="w-full md:w-auto">
              <h3 className="text-lg font-semibold mb-2">상품 상태 변경</h3>
              <div className="flex gap-2 flex-wrap">
                <Button
                  className={`px-4 py-2 rounded font-bold transition-colors shadow ${
                    post.status === "AVAILABLE"
                      ? "bg-green-700 text-white ring-2 ring-green-900"
                      : "bg-green-200 text-green-900 hover:bg-green-300"
                  }`}
                  onClick={() => handleStatusChange("AVAILABLE")}
                  disabled={statusLoading || post.status === "AVAILABLE"}
                >
                  판매중
                </Button>

                <Button
                  className={`px-4 py-2 rounded font-bold transition-colors shadow ${
                    post.status === "RESERVED"
                      ? "bg-yellow-500 text-white ring-2 ring-yellow-700"
                      : "bg-yellow-200 text-yellow-900 hover:bg-yellow-300"
                  }`}
                  onClick={() => handleStatusChange("RESERVED")}
                  disabled={statusLoading || post.status === "RESERVED"}
                >
                  예약중
                </Button>

                <Button
                  className={`px-4 py-2 rounded font-bold transition-colors shadow ${
                    post.status === "PURCHASED"
                      ? "bg-red-700 text-white ring-2 ring-red-900"
                      : "bg-red-200 text-red-900 hover:bg-red-300"
                  }`}
                  onClick={() => handleStatusChange("PURCHASED")}
                  disabled={statusLoading || post.status === "PURCHASED"}
                >
                  판매 완료
                </Button>
              </div>
            </div>
          )}

          {isLogin && (
            <div className="flex flex-wrap gap-3 justify-end w-full md:w-auto">
              <Button
                disabled={likeLoading || liked}
                onClick={handleLike}
                className="px-4 py-2 bg-gray-700 text-white rounded"
              >
                {liked ? "찜 완료" : likeLoading ? "처리 중..." : "찜하기"}
              </Button>
              <Button
                disabled={purchaseLoading || purchased}
                onClick={handlePurchase}
                className="px-4 py-2 bg-gray-700 text-white rounded"
              >
                {purchased
                  ? "구매 완료"
                  : purchaseLoading
                  ? "처리 중..."
                  : "구매하기"}
              </Button>
              {loginMember.id === post.writerId && (
                <>
                  <Button
                    onClick={handleEdit}
                    className="px-4 py-2 bg-gray-700 text-white rounded"
                  >
                    수정하기
                  </Button>
                  <Button
                    onClick={handleDelete}
                    className="px-4 py-2 bg-gray-700 text-white rounded"
                  >
                    삭제하기
                  </Button>
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {isLogin && (
        <div className="w-full border-t pt-6 mt-10">
          <h3 className="text-xl font-bold mb-4">댓글</h3>
          <Comments
            postId={post.id!}
            initialComments={comments}
            loadMoreComments={loadComments}
          />
        </div>
      )}

      {/* 거래 위치 모달 (열림 상태에 따라 렌더링) */}
      {tradeModalOpen && (
        <TradeLocationModal
          lat={post.latitude}
          lng={post.longitude}
          zoom={16} // 필요에 따라 줌 레벨 조정
          address={(post as any).location || "등록된 주소 없음"}
          onClose={() => setTradeModalOpen(false)}
        />
      )}
    </div>
  );
}
