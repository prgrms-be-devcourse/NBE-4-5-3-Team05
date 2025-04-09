"use client";

import { useEventSource } from "@/app/stores/notification/notificationSessionStore";
import { escape } from "querystring";
import { useEffect } from "react";
import { toast, ToastContainer } from "react-toastify";

export default function TostNotification() {
  const EventSourceContext = useEventSource();
  useEffect(() => {
    const init = async () => {
      if (typeof window !== "undefined") {
        const es = await EventSourceContext.connect(
          "http://localhost:8080/api/notification/subscribe"
        );
        // 서버의 SSE 엔드포인트 (예시)

        es.addEventListener("kafka-event", (event) => {
          console.log("event:", event.data);
          toast.info(`${event.data}`);
        });
        es.onmessage = (event) => {
          // event.data 는 서버에서 보내준 메시지 (문자열)

          // 토스트로 표시
          toast.info(`${event.data}`);
        };

        es.onerror = (err) => {
          console.error("SSE error:", err);
          // 문제가 발생하면 연결 닫기
          EventSourceContext.disconnect();
        };

        // 컴포넌트 unmount 시에는 반드시 연결 종료
        return () => {
          EventSourceContext.disconnect();
        };
      }
    };

    init();
  }, []);
  return (
    <div>
      {/* 토스트 표시 컨테이너 */}
      <ToastContainer />
    </div>
  );
}
