package com.NBE_4_5_2.Team5.domain.post.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductMetadata;

public interface ProductMetadataRepository extends JpaRepository<ProductMetadata, String> {
	List<ProductMetadata> findByName(String name);
}