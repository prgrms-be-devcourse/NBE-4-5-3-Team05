package com.NBE_4_5_2.Team5.domain.admin.dto;

import java.time.LocalDateTime;

import com.NBE_4_5_2.Team5.domain.admin.entity.BanList;
import com.NBE_4_5_2.Team5.domain.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class BanListDto {
	private String id;
	private String reason;
	private UserDto user;
	private LocalDateTime startDate;
	private LocalDateTime endDate;

	public BanListDto(BanList saved) {
		this.id = saved.getId();
		this.reason = saved.getReason();
		this.user = new UserDto(saved.getBannedUser());
		this.startDate = saved.getStartDate();
		this.endDate = saved.getEndDate();
	}
}
