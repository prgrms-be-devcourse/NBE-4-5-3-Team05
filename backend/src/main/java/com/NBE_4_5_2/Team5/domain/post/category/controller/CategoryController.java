package com.NBE_4_5_2.Team5.domain.post.category.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.category.service.CategoryService;
import com.NBE_4_5_2.Team5.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "카테고리 API")
public class CategoryController {

	private final CategoryService categoryService;

	@Operation(summary = "카테고리 작성", description = "존재하는 카테고리 목록을 조회합니다.")
	@GetMapping
	public RsData<List<Category>> getCategories() {
		List<Category> categories = categoryService.getCategories();

		return new RsData<>(
			"200",
			"카테고리 목록을 조회합니다.",
			categories
		);
	}
}
