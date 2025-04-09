package util

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class Util {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun truncateAllTables() {
        // 외래키 제약조건 비활성화
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()

        // PUBLIC 스키마에 있는 모든 테이블 이름 조회 (H2의 기본 스키마는 PUBLIC)
        val tableNames = entityManager
            .createNativeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'")
            .resultList as List<String>

        // 각 테이블을 TRUNCATE
        tableNames.forEach { tableName ->
            entityManager.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
        }

        // 외래키 제약조건 재활성화
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }
}
