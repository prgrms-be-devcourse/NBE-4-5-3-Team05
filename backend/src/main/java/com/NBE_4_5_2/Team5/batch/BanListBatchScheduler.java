package com.NBE_4_5_2.Team5.batch;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.NBE_4_5_2.Team5.domain.user.admin.entity.BanList;
import com.NBE_4_5_2.Team5.domain.user.admin.repository.BanListRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BanListBatchScheduler {

	private final BanListRepository banListRepository;

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void schedule() {
		List<BanList> allByEndDateAfter = banListRepository.findAllByEndDateAfter(LocalDate.now().atStartOfDay());
		allByEndDateAfter.forEach(item -> item.getBannedUser().unBan());
	}
}
