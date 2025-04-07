package com.NBE_4_5_2.Team5.domain.post.category.repository;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
