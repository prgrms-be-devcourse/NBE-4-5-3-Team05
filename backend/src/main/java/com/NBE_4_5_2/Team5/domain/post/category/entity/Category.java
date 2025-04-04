package com.NBE_4_5_2.Team5.domain.post.category.entity;

import com.NBE_4_5_2.Team5.domain.base.entity.BaseLongIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseLongIdEntity {

	@Column(nullable = false, unique = true)
	private String name;

	public Category(Long id, String name){
		setId(id);
		this.name = name;
	}
}
