package com.NBE_4_5_2.Team5.domain.post.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class ProductMetadata {

	@Id
	private String name;
	@Column(name = "metadata_value")
	private String value;

	public void setValue(String value) {
		this.value = value;
	}

	public ProductMetadata(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public ProductMetadata() {
	}
}
