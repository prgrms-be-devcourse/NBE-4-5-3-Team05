package com.NBE_4_5_2.Team5.domain.payment.service

import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentDto
import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentMetaData
import com.NBE_4_5_2.Team5.domain.payment.entity.Payment
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus
import com.NBE_4_5_2.Team5.domain.payment.repository.PaymentRepository
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.exception.payment.PaymentChargeException
import com.NBE_4_5_2.Team5.global.exception.payment.PaymentNotFoundException
import com.NBE_4_5_2.Team5.global.exception.post.product.ProductPostNotFoundException
import jakarta.validation.constraints.NotNull
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Slf4j
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val productRepository: ProductPostRepository,
    private val paymentProviderAdapter: PaymentProviderAdapter,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val productPostRepository: ProductPostRepository,
) {
    private val loggedInUser: User
        get() = userService.userIdentity

    fun saveMetaData(paymentId: String, amount: Int): PaymentMetaData {
        val user = loggedInUser
        return paymentRepository.save(
            Payment(
                paymentId,
                user,
                amount
            )
        ).let{
            PaymentMetaData(it)
        }
    }


    fun purchase(productId: String): PaymentDto {
        // TODO : userRepository가 optional 반환안하면 !!붙이기
        val loggedInUserEntity = userRepository.findById(loggedInUser.id).get()
        val product = productRepository.findById(productId)
            .orElseThrow {
                PaymentNotFoundException(
                    "404",
                    "id가 $productId 인 Payment를 찾을 수 없습니다."
                )
            }

        // 할인 등 product 가격과 총 결제 금액이 다를 수 있으므로 amount를 따로 받음.
        loggedInUserEntity.canBuy(product, product.productPrice)
        loggedInUserEntity.buy(product, product.productPrice)

        val purchasedPayment = Payment(
            loggedInUser,
            -1 * product.productPrice,
            PaymentStatus.DONE
        )

        purchasedPayment.updatePaymentKey(productId)

        val saved = paymentRepository.save(purchasedPayment)

        return PaymentDto.of(saved)
    }

    fun requestCharge(id: @NotNull String, paymentKey: @NotNull String?, amount: @NotNull Int) {
        val loggedInUser = loggedInUser

        val loggedInUserEntity = userRepository.findById(loggedInUser.id).get()
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException("404", "Payment를 찾을 수 없습니다.") }

        if (!payment.checkValid(amount)) {
            throw PaymentChargeException("404", "총 가격이 맞지 않습니다.")
        }

        val update = payment.updatePaymentKey(paymentKey)

        try {
            paymentProviderAdapter.requestPayment(id, paymentKey, amount)

            loggedInUserEntity.chargeCash(amount)

            payment.updateState(PaymentStatus.DONE)
        } catch (e: RuntimeException) {
            log.error(e.message)
        }
    }

    fun isPurchased(postId: String): Boolean {
        val loggedInUser = loggedInUser

        val productPost = productPostRepository.findById(postId)
            .orElseThrow { ProductPostNotFoundException("404", "product post를 찾을 수 없습니다.") }

        return productPost.isPurchasedBy(loggedInUser)
    }
}
