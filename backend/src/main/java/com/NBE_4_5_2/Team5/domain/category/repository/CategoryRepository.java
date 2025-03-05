package com.NBE_4_5_2.Team5.domain.category.repository;

import com.NBE_4_5_2.Team5.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
}
