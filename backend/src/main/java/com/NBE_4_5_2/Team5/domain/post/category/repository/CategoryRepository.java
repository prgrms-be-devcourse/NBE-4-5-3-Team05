package com.NBE_4_5_2.Team5.domain.post.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
