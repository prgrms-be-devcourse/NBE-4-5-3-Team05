package com.NBE_4_5_2.Team5.domain.post.category.controller;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.category.service.CategoryService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

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
