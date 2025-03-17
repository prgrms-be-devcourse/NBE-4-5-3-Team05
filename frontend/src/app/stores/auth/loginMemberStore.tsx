"use client";

import { components } from "@/lib/backend/apiV1/schema";
import { createContext, useState } from "react";

type Member = components["schemas"]["UserDto"];

export const LoginMemberContext = createContext<{
  loginMember: Member;
  setLoginMember: (member: Member) => void;
  removeLoginMember: () => void;
  isLogin: boolean;
  isLoginMemberPending: boolean;
  isAdmin: boolean;
  setNoLoginMember: () => void;
}>({
  loginMember: createEmptyMember(),
  setLoginMember: () => {},
  removeLoginMember: () => {},
  isLogin: false,
  isLoginMemberPending: true,
  isAdmin: false,
  setNoLoginMember: () => {},
});

function createEmptyMember(): Member {
  return {
    id: "",
    username: "",
    email: "",
    cash: 0,
    nickname: "",
    address: "",
    profileUrl: "",
    role: "USER",
    createdAt: "",
    modifiedAt: "",
    blocked: false,
    blockedCount: 0,
  };
}

export function useLoginMember() {
  const [isLoginMemberPending, setLoginMemberPending] = useState(true);
  const [loginMember, _setLoginMember] = useState<Member>(createEmptyMember());

  const removeLoginMember = () => {
    _setLoginMember(createEmptyMember());
    setLoginMemberPending(false);
  };

  const setLoginMember = (member: Member) => {
    _setLoginMember(member);
    setLoginMemberPending(false);
  };

  const setNoLoginMember = () => {
    setLoginMemberPending(false);
  };

  const isLogin = loginMember.id !== "";
  const isAdmin = loginMember.role === "ADMIN";

  return {
    loginMember,
    removeLoginMember,
    isLogin,
    isLoginMemberPending,
    setLoginMember,
    isAdmin,
    setNoLoginMember,
  };
}
