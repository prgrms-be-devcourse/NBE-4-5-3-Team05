import createClient from "openapi-fetch";
import { paths } from "./backend/apiV1/schema";

const fileUploadClient = createClient<paths>({
  baseUrl: `http://${process.env.NEXT_PUBLIC_BACKEND_HOST}:${process.env.NEXT_PUBLIC_BACKEND_PORT}`,
  headers: {},
  // fetch 함수의 시그니처를 (input: RequestInfo, init?: RequestInit) => Promise<Response>로 맞춥니다.
  fetch: async (input: RequestInfo, init?: RequestInit): Promise<Response> => {
    const response = await fetch(input, init);
    // 응답 본문을 텍스트로 읽습니다.
    const text = await response.text();
    // 새 Response 객체를 생성합니다.
    const modifiedResponse = new Response(text, {
      status: response.status,
      statusText: response.statusText,
      headers: response.headers,
    });
    // modifiedResponse.json()을 오버라이드하여, JSON 파싱 대신 텍스트를 그대로 반환하게 합니다.
    modifiedResponse.json = async () => text;
    return modifiedResponse;
  },
});

export default fileUploadClient;
