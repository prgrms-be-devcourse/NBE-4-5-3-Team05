package com.NBE_4_5_2.Team5.domain.post.post.repository;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPostRepository extends JpaRepository<ProductPost, String> {

    Page<ProductPost> findByTitleLike(String title, Pageable pageable);
}
