package com.NBE_4_5_2.Team5.domain.product.repository;

import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findAllByStatus(String status);

    List<Product> findAllBySellerIdAndStatus(String sellerId, String status);
}
