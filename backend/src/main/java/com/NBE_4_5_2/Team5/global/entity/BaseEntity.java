package com.NBE_4_5_2.Team5.global.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BaseEntity {

    @Id
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private String id;

}
