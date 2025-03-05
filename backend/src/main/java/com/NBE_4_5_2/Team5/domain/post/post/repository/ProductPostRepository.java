package com.NBE_4_5_2.Team5.domain.post.post.repository;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPostRepository extends JpaRepository<ProductPost, String> {

    @EntityGraph(attributePaths = {"productCategories.category"})
    Page<ProductPost> findByTitleLike(String title, Pageable pageable);


    @EntityGraph(attributePaths = {"productCategories.category"})
    @Query("select p from ProductPost p")
    Page<ProductPost> findAllWithCategories(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"productCategories.category"})
    Page<ProductPost> findByWriter(User writer, Pageable pageable);
}
