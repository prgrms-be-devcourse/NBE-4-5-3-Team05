import createClient from "openapi-fetch";
import { paths } from "./backend/apiV1/schema";

let baseUrl = `${process.env.NEXT_PUBLIC_PROTOCOL}://${process.env.NEXT_PUBLIC_BACKEND_HOST}`;
if (`${process.env.NEXT_PUBLIC_PROTOCOL}` === "https") {
} else {
  baseUrl += `:${process.env.NEXT_PUBLIC_BACKEND_PORT}`;
}
const client = createClient<paths>({
  baseUrl: baseUrl,
  headers: {
    "Content-Type": "application/json",
  },
});

export default client;
