package com.NBE_4_5_2.Team5.domain.product.entity;

import java.util.UUID;

import com.NBE_4_5_2.Team5.domain.product.dto.ProductStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_post")
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Product {

	@Id
	private final String id = "ppost-" + UUID.randomUUID();

	@Enumerated(EnumType.STRING)
	private ProductStatus status = ProductStatus.AVAILABLE;

	private Integer price;

	public boolean isAvailable() {
		return status == ProductStatus.AVAILABLE;
	}

	public void updateStatus(ProductStatus status) {
		this.status = status;
	}
}
