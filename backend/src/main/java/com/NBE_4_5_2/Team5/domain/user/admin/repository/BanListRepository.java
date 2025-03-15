package com.NBE_4_5_2.Team5.domain.user.admin.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList;

public interface BanListRepository extends JpaRepository<BanList, String> {
	List<BanList> findAllByEndDateAfter(LocalDateTime endDateAfter);
}