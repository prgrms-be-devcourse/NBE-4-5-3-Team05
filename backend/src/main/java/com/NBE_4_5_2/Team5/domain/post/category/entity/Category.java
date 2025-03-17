package com.NBE_4_5_2.Team5.domain.post.category.entity;

import com.NBE_4_5_2.Team5.domain.base.entity.BaseLongIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseLongIdEntity {

	@Column(nullable = false, unique = true)
	private String name;
}
