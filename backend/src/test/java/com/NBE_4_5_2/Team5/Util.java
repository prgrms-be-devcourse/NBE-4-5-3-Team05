package com.NBE_4_5_2.Team5;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

public class Util {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void truncateAllTables() {
		// 외래키 제약조건 비활성화
		entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

		// PUBLIC 스키마에 있는 모든 테이블 이름 조회 (H2의 기본 스키마는 PUBLIC)
		List<String> tableNames = entityManager.createNativeQuery(
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'")
			.getResultList();

		// 각 테이블을 TRUNCATE
		for (String tableName : tableNames) {
			entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
		}

		// 외래키 제약조건 재활성화
		entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
	}

}
