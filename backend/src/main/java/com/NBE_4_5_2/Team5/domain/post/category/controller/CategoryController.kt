package com.NBE_4_5_2.Team5.domain.post.category.controller

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category
import com.NBE_4_5_2.Team5.domain.post.category.service.CategoryService
import com.NBE_4_5_2.Team5.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category API", description = "카테고리 API")
class CategoryController(
    private val categoryService: CategoryService
) {

    @Operation(summary = "카테고리 작성", description = "존재하는 카테고리 목록을 조회합니다.")
    @GetMapping
    fun getCategories(): RsData<List<Category>> {
        val categories = categoryService.categories
        return RsData(
            "200",
            "카테고리 목록을 조회합니다.",
            categories
        )
    }
}
