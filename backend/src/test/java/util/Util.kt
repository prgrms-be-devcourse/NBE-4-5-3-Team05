package util

import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

class Util {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Throws(IOException::class)
    fun paymentRequestResponse(uuid: String?, paymentKey: String?, totalAmount: Int?): Map<String?, Any?> {
        val bodyString = """
			{
			  "mId": "tosspayments",
			  "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E",
			  "paymentKey": "$paymentKey",
			  "orderId": "$uuid",
			  "orderName": "토스 티셔츠 외 2건",
			  "taxExemptionAmount": 0,
			  "status": "DONE",
			  "requestedAt": "2024-02-13T12:17:57+09:00",
			  "approvedAt": "2024-02-13T12:18:14+09:00",
			  "useEscrow": false,
			  "cultureExpense": false,
			  "card": {
			    "issuerCode": "71",
			    "acquirerCode": "71",
			    "number": "12345678****000*",
			    "installmentPlanMonths": 0,
			    "isInterestFree": false,
			    "interestPayer": null,
			    "approveNo": "00000000",
			    "useCardPoint": false,
			    "cardType": "신용",
			    "ownerType": "개인",
			    "acquireStatus": "READY",
			    "receiptUrl": "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tviva20240213121757MvuS8&ref=PX",
			    "amount": 1000
			  },
			  "virtualAccount": null,
			  "transfer": null,
			  "mobilePhone": null,
			  "giftCertificate": null,
			  "cashReceipt": null,
			  "cashReceipts": null,
			  "discount": null,
			  "cancels": null,
			  "secret": null,
			  "type": "NORMAL",
			  "easyPay": {
			    "provider": "토스페이",
			    "amount": 0,
			    "discountAmount": 0
			  },
			  "country": "KR",
			  "failure": null,
			  "isPartialCancelable": true,
			  "receipt": {
			    "url": "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tviva20240213121757MvuS8&ref=PX"
			  },
			  "checkout": {
			    "url": "https://api.tosspayments.com/v1/payments/5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1/checkout"
			  },
			  "currency": "KRW",
			  "totalAmount": $totalAmount},
			  "balanceAmount": 1000,
			  "suppliedAmount": 909,
			  "vat": 91,
			  "taxFreeAmount": 0,
			  "metadata": null,
			  "method": "카드",
			  "version": "2022-11-16"
			}
			
			""".trimIndent()
        return objectMapper.convertValue(objectMapper.readTree(bodyString), Map::class.java) as Map<String?, Any?>
    }

//    @PersistenceContext
//    lateinit var entityManager: EntityManager

//    fun truncateAllTables() {
//        // 외래키 제약조건 비활성화
//        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()
//
//        // PUBLIC 스키마에 있는 모든 테이블 이름 조회 (H2의 기본 스키마는 PUBLIC)
//        val tableNames = entityManager
//            .createNativeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'")
//            .resultList as List<String>
//
//        // 각 테이블을 TRUNCATE
//        tableNames.forEach { tableName ->
//            entityManager.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
//        }
//
//        // 외래키 제약조건 재활성화
//        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
//    }
}
