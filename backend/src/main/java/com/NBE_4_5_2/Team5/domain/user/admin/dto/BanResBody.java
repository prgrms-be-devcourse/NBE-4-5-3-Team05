package com.NBE_4_5_2.Team5.domain.user.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class BanResBody {
	private String banListId;
	private String userId;
	private String reason;
	private Integer banCount;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
}
