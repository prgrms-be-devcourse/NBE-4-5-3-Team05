package com.NBE_4_5_2.Team5.infrastructure.toss

import com.NBE_4_5_2.Team5.domain.payment.service.PaymentProviderAdapter
import com.NBE_4_5_2.Team5.global.exception.TossPaymentException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class TossPaymentProviderAdapter(
    @Value("\${custom.toss.payment.secret}")
    private val tossPaymentSecretKey: String
) : PaymentProviderAdapter {

    // 한 번만 생성해서 재사용
    private val restClient: RestClient = RestClient.create()

    // 요청 바디용 data class
    private data class ConfirmPaymentRequest(
        val paymentKey: String,
        val orderId: String,
        val amount: Int
    )

    override fun requestPayment(
        id: String,
        paymentKey: String,
        amount: Int
    ): ResponseEntity<Map<String, Any>> {
        // 1) 바디 준비
        val body = ConfirmPaymentRequest(paymentKey, id, amount)

        // 2) 호출
        val response: ResponseEntity<Map<String, Any>> = restClient.post()
            .uri("https://api.tosspayments.com/v1/payments/confirm")
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic $tossPaymentSecretKey")
            .body(body)
            .retrieve()
            // 제네릭 타입 안전하게 처리
            .toEntity(object : ParameterizedTypeReference<Map<String, Any>>() {})

        // 3) 오류 체크
        if (response.statusCode != HttpStatus.OK || response.body == null) {
            throw TossPaymentException("결제 승인 요청에 실패했습니다.")
        }

        return response
    }
}
