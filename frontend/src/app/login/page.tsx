"use client";

import { useState } from "react";

export default function Page() {
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const login = async () => {
    const response = await fetch("http://localhost:8080/api/users/login", {
      body: JSON.stringify({
        username,
        password,
      }),
      method: "post",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
    });
  };
  return (
    <div>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          login();
        }}
      >
        <label htmlFor="username">username</label>
        <input
          id="username"
          name="username"
          value={username}
          onChange={(e) => {
            e.preventDefault();
            setUsername(e.target.value);
          }}
        />
        <label htmlFor="password">password</label>
        <input
          id="password"
          name="password"
          value={password}
          onChange={(e) => {
            e.preventDefault();
            setPassword(e.target.value);
          }}
        />
        <input type="submit" value="submit" />
      </form>
    </div>
  );
}
