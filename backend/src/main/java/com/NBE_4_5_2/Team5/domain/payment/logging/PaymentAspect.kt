package com.NBE_4_5_2.Team5.domain.payment.logging

import com.NBE_4_5_2.Team5.domain.payment.controller.PaymentController.PurchaseItemReqDto
import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentDto
import com.NBE_4_5_2.Team5.domain.payment.dto.PaymentMetaData
import com.NBE_4_5_2.Team5.global.response.RsData
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
class PaymentAspect(
    private val request: HttpServletRequest // 클라이언트의 요청 정보를 얻기 위한 객체 주입
) {

    private val logger = LoggerFactory.getLogger(PaymentAspect::class.java)

    /**
     * 결제 메타데이터 저장(saveMetaData) API 실행 전후에 로그를 남깁니다.
     *
     * 로그 내용:
     * - 시작 시각, 클라이언트 IP, 결제에 사용될 payment id와 결제 금액(amount)
     * - API 처리 결과가 RsData<PaymentMetaData> 형태이면, 저장된 결제 메타데이터의 정보를 로그로 출력
     * - 예외 발생 시 에러 메시지를 기록
     *
     * 대상 메소드: PaymentController.saveMetaData(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.payment.controller.PaymentController.saveMetaData(..))")
    fun logSaveMetaData(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 첫 번째 파라미터는 결제에 사용될 상품 게시글 id (paymentId로 활용)
        val paymentId = args[0] as String
        // 두 번째 파라미터는 결제 금액
        val amount = args[1] as Int
        val ip = request.remoteAddr

        logger.info("[{}] [META SAVE START] 사용자IP: {}, paymentId: {}, amount: {}",
            now, ip, paymentId, amount)

        return try {
            val result = joinPoint.proceed()
            if (result is RsData<*>) {
                // 결과값이 RsData<PaymentMetaData>인 경우 결제 메타데이터 내 paymentId와 amount 값을 로그에 남김
                val metadata = result.data as? PaymentMetaData
                if (metadata != null) {
                    logger.info("[{}] [META SAVE END] 저장 완료 - paymentId: {}, amount: {}",
                        now, metadata.paymentId, metadata.amount)
                } else {
                    logger.info("[{}] [META SAVE END] 저장 완료", now)
                }
            }
            result
        } catch (e: Throwable) {
            logger.error("[{}] [META SAVE ERROR] 메타데이터 저장 중 에러 발생: {}",
                now, e.message)
            throw e
        }
    }

    /**
     * 결제 승인(requestPayment) API 실행 전후에 로그를 남깁니다.
     *
     * 로그 내용:
     * - 시작 시각, 클라이언트 IP, 주문 ID(orderId), PG사가 생성한 paymentKey, 총 결제 금액(amount)
     * - 성공 시 승인 요청 완료 로그를 남김
     * - 예외 발생 시 에러 메시지 로그 기록
     *
     * 대상 메소드: PaymentController.requestPayment(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.payment.controller.PaymentController.requestPayment(..))")
    fun logRequestPayment(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 파라미터 순서: orderId, paymentKey, amount
        val orderId = args[0] as String
        val paymentKey = args[1] as String
        val amount = args[2] as Int
        val ip = request.remoteAddr

        logger.info("[{}] [PAYMENT REQUEST START] 사용자IP: {}, orderId: {}, paymentKey: {}, amount: {}",
            now, ip, orderId, paymentKey, amount)

        return try {
            val result = joinPoint.proceed()
            logger.info("[{}] [PAYMENT REQUEST END] 결제 승인 요청 성공 - orderId: {}",
                now, orderId)
            result
        } catch (e: Throwable) {
            logger.error("[{}] [PAYMENT REQUEST ERROR] 결제 승인 요청 중 에러 발생 (orderId: {}): {}",
                now, orderId, e.message)
            throw e
        }
    }

    /**
     * 상품 구매(purchaseItem) API 실행 전후에 로그를 남깁니다.
     *
     * 로그 내용:
     * - 시작 시각, 클라이언트 IP, 구매할 상품 게시글 id를 로그에 기록합니다.
     * - API 처리 결과가 RsData<PaymentDto> 형태이면, 구매에 대한 Payment ID, 구매자, 총 결제 금액 등의 정보를 로그로 출력합니다.
     * - 예외 발생 시 에러 메시지를 기록합니다.
     *
     * 대상 메소드: PaymentController.purchaseItem(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.payment.controller.PaymentController.purchaseItem(..))")
    fun logPurchaseItem(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 직접 캐스팅을 통해 PurchaseItemReqDto에서 구매할 상품 게시글 id를 추출합니다.
        val purchaseReq = args[0] as PurchaseItemReqDto
        val productId = purchaseReq.productId
        val ip = request.remoteAddr

        logger.info("[{}] [PURCHASE START] 사용자IP: {}, 구매 요청 상품 ID: {}",
            now, ip, productId)

        return try {
            val result = joinPoint.proceed()
            if (result is RsData<*>) {
                val paymentDto = result.data as? PaymentDto
                if (paymentDto != null) {
                    logger.info("[{}] [PURCHASE END] 구매 완료 - Payment ID: {}, 구매자: {}, 총 금액: {}",
                        now, paymentDto.id, paymentDto.buyerId, paymentDto.totalPrice)
                } else {
                    logger.info("[{}] [PURCHASE END] 구매 완료", now)
                }
            }
            result
        } catch (e: Throwable) {
            logger.error("[{}] [PURCHASE ERROR] 상품 구매 중 에러 발생: {}",
                now, e.message)
            throw e
        }
    }

    /**
     * 상품 구매 여부(checkPurchased) API 실행 전후에 로그를 남깁니다.
     *
     * 로그 내용:
     * - 시작 시각, 클라이언트 IP, 확인 대상 상품 게시글 id를 로그에 기록합니다.
     * - API 처리 후 구매 여부(Boolean)를 로그에 출력합니다.
     * - 예외 발생 시 에러 메시지를 기록합니다.
     *
     * 대상 메소드: PaymentController.checkPurchased(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.payment.controller.PaymentController.checkPurchased(..))")
    fun logCheckPurchased(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 파라미터 중 "post-id"로 전달된 값이 대상입니다.
        val postId = args[0] as String
        val ip = request.remoteAddr

        logger.info("[{}] [CHECK PURCHASED START] 사용자IP: {}, 확인 대상 상품 게시글 ID: {}",
            now, ip, postId)

        return try {
            val result = joinPoint.proceed()
            if (result is RsData<*>) {
                val purchased = result.data as? Boolean
                logger.info("[{}] [CHECK PURCHASED END] 구매 여부: {} (상품 게시글 ID: {})",
                    now, purchased, postId)
            } else {
                logger.info("[{}] [CHECK PURCHASED END] 완료 (상품 게시글 ID: {})", now, postId)
            }
            result
        } catch (e: Throwable) {
            logger.error("[{}] [CHECK PURCHASED ERROR] 구매 여부 조회 중 에러 발생 (상품 게시글 ID: {}): {}",
                now, postId, e.message)
            throw e
        }
    }
}
