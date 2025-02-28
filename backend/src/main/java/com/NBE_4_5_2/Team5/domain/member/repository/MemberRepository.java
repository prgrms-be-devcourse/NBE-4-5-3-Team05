package com.NBE_4_5_2.Team5.domain.member.repository;

import com.NBE_4_5_2.Team5.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {
}
