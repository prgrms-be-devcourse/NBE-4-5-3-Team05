package com.NBE_4_5_2.Team5.domain.base.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuperBuilder
public class BaseEntity {

	@Id
	@EqualsAndHashCode.Include
	private String id;

}
