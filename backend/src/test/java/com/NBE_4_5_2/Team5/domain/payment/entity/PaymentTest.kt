package com.NBE_4_5_2.Team5.domain.payment.entity

import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.util.ReflectionTestUtils

class PaymentTest {

    @Test
    fun updateStateTest() {
        val dummyBuyer = Mockito.mock(User::class.java)
        val payment = Payment(dummyBuyer, 0, PaymentStatus.IN_PROGRESS)

        payment.updateState(PaymentStatus.DONE)

        assertThat(payment.status).isEqualTo(PaymentStatus.DONE)
    }

    @Test
    fun checkValid() {
        val dummyBuyer = Mockito.mock(User::class.java)
        val payment = Payment(dummyBuyer, 0, PaymentStatus.IN_PROGRESS)
        val metadataValue = 50000
        ReflectionTestUtils.setField(payment, "_totalPrice", metadataValue)

        val clientValue = 50000
        val res = payment.checkValid(clientValue)

        assertThat(res).isTrue()
    }
}
