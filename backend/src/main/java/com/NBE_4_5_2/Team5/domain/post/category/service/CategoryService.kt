package com.NBE_4_5_2.Team5.domain.post.category.service

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    val categoryRepository: CategoryRepository
) {

    val categories: List<Category>
        get() = categoryRepository.findAll()
}
