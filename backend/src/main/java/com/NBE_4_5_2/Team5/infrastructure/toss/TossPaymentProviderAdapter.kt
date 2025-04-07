package com.NBE_4_5_2.Team5.infrastructure.toss

import com.NBE_4_5_2.Team5.domain.payment.service.PaymentProviderAdapter
import com.NBE_4_5_2.Team5.global.exception.TossPaymentException
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
@RequiredArgsConstructor
class TossPaymentProviderAdapter : PaymentProviderAdapter {
    @Value("\${custom.toss.payment.secret}")
    private val tossPaymentSecretKey: String? = null

    private class TossPaymentReqBody(
        var paymentKey: String? = null,
        var orderId: String? = null,
        var amount:Int = 0
    ) {
    }

    override fun requestPayment(id: String, paymentKey: String, amount: Int): ResponseEntity<Map<String?, Any?>?>? {
        val restClient = RestClient.create()
        val response: ResponseEntity<MutableMap<*, *>> = restClient.post()
            .uri("https://api.tosspayments.com/v1/payments/confirm")
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic $tossPaymentSecretKey")
            .body(TossPaymentReqBody(paymentKey, id, amount))
            .retrieve()
            .toEntity(MutableMap::class.java)

        if (response.statusCode != HttpStatus.OK || !response.hasBody()) {
            throw TossPaymentException("결제 승인 요청에 실패했습니다.")
        }

        val body = checkNotNull(response.body as Map<String?, Any?>?)
        return ResponseEntity(body, response.statusCode)
    }


}
