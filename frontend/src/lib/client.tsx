import createClient from "openapi-fetch";
import { paths } from "./backend/apiV1/schema";

const client = createClient<paths>({
  baseUrl: `http://${process.env.NEXT_PUBLIC_BACKEND_HOST}:${process.env.NEXT_PUBLIC_BACKEND_PORT}`,
  headers: {
    "Content-Type": "application/json",
  },
});

export default client;
