package com.NBE_4_5_2.Team5.global.response;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageDto<T> {
	// 상품 목록, 주문 목록

	@NonNull
	List<T> items;

	@NonNull
	private int totalPages;

	@NonNull
	private int totalItems;

	@NonNull
	private int curPageNo;

	@NonNull
	private int pageSize;

	public PageDto(Page<T> page) {
		this.items = page.getContent();
		this.totalItems = (int)page.getTotalElements();
		this.totalPages = page.getTotalPages();
		this.curPageNo = page.getNumber() + 1;
		this.pageSize = page.getSize();
	}

}

