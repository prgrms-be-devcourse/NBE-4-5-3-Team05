package com.NBE_4_5_2.Team5.domain.post.post.logging

import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostModifyForm
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostWriteForm
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.global.dto.RsData
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Aspect
@Component
class PostAspect(
    private val request: HttpServletRequest  // 클라이언트의 요청 정보를 얻기 위한 객체 주입
) {

    // 로그 기록을 위한 SLF4J Logger 객체 생성
    private val logger = LoggerFactory.getLogger(PostAspect::class.java)

    /**
     * 상품 게시글 생성(createPost) 메소드 실행 전후에 로그를 기록합니다.
     *
     * 로그 내용:
     * - 실행 시작 시각과 클라이언트 IP, 입력된 상품 정보(상품명, 가격)
     * - 실행 성공 시 게시글 생성 결과의 ID를 포함
     * - 예외 발생 시 에러 메시지를 기록
     *
     * 메소드 대상: ProductPostController.createPost(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.post.post.controller.ProductPostController.createPost(..))")
    fun logCreatePost(joinPoint: ProceedingJoinPoint): Any? {
        // Aspect 실행 시각 기록
        val now = LocalDateTime.now()
        // 메소드의 인수(argument) 배열 추출. 여기서는 ProductPostWriteForm이 첫 번째 인수로 전달됨
        val args = joinPoint.args
        val writeForm = args[0] as ProductPostWriteForm
        // 클라이언트 IP 주소 추출
        val ip = request.remoteAddr
        // 시작 로그 기록: 현재 시간, IP, 상품명, 상품 가격을 출력
        logger.info("[{}] [CREATE START] 사용자IP: {}, 상품명: {}, 가격: {}",
            now, ip, writeForm.productName, writeForm.productPrice)

        try {
            // 실제 메소드 실행
            val result = joinPoint.proceed()
            // 결과가 RsData<ProductPostResponse> 형태인 경우 데이터 내 게시글 ID 추출
            val rsData = result as RsData<*>
            if (rsData.data is ProductPostResponse) {
                val postResponse = rsData.data as ProductPostResponse
                // 성공 로그 기록: 게시글 ID를 출력
                logger.info("[{}] [CREATE END] 작성 완료 - 게시글 ID: {}", now, postResponse.id)
            } else {
                // 결과 데이터 형식이 예상과 다를 경우 단순 완료 로그 기록
                logger.info("[{}] [CREATE END] 작성 완료", now)
            }
            return result
        } catch (e: Throwable) {
            // 예외 발생 시 에러 로그 기록: 발생 시각과 에러 메시지를 출력
            logger.error("[{}] [CREATE ERROR] 작성 중 에러 발생: {}", now, e.message)
            // 예외를 다시 던져 상위 로직에서 처리할 수 있도록 함
            throw e
        }
    }

    /**
     * 상품 게시글 수정(modify) 메소드 실행 전후에 로그를 기록합니다.
     *
     * 로그 내용:
     * - 시작 시각, 클라이언트 IP, 수정 대상 게시글 ID, 그리고 변경될 상품명 등
     * - 실행 성공 시 수정 완료 메시지와 게시글 ID
     * - 예외 발생 시 에러 메시지 포함 로그 기록
     *
     * 메소드 대상: ProductPostController.modify(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.post.post.controller.ProductPostController.modify(..))")
    fun logModifyPost(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 첫 번째 인자는 수정용 폼(ProductPostModifyForm), 두 번째 인자는 게시글 ID
        val modifyForm = args[0] as ProductPostModifyForm
        val postId = args[1] as String
        val ip = request.remoteAddr
        // 수정 시작 로그 기록: 시각, IP, 게시글 ID 및 수정할 상품명 출력
        logger.info("[{}] [MODIFY START] 사용자IP: {}, 수정 대상 게시글 ID: {}, 신규 상품명: {}",
            now, ip, postId, modifyForm.productName)

        try {
            val result = joinPoint.proceed()
            // 실행 후 수정 완료 로그 기록: 현재 시각과 게시글 ID 출력
            logger.info("[{}] [MODIFY END] 수정 완료 - 게시글 ID: {}", now, postId)
            return result
        } catch (e: Throwable) {
            // 예외 발생 시 오류 로그 기록: 게시글 ID와 에러 메시지를 포함
            logger.error("[{}] [MODIFY ERROR] 수정 중 에러 발생 (게시글 ID: {}): {}",
                now, postId, e.message)
            throw e
        }
    }

    /**
     * 상품 게시글 삭제(delete) 메소드 실행 전후에 로그를 기록합니다.
     *
     * 로그 내용:
     * - 시작 시각, 클라이언트 IP, 삭제할 게시글 ID 기록
     * - 성공 시 삭제 완료 메시지와 게시글 ID 기록
     * - 예외 발생 시 상세한 에러 메시지 기록
     *
     * 메소드 대상: ProductPostController.delete(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.post.post.controller.ProductPostController.delete(..))")
    fun logDeletePost(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 삭제할 게시글의 ID는 첫 번째 인수로 전달됨
        val postId = args[0] as String
        val ip = request.remoteAddr
        // 삭제 시작 로그: 시각, IP, 게시글 ID 출력
        logger.info("[{}] [DELETE START] 사용자IP: {}, 삭제 대상 게시글 ID: {}",
            now, ip, postId)

        try {
            val result = joinPoint.proceed()
            // 삭제 완료 후 성공 로그 기록
            logger.info("[{}] [DELETE END] 삭제 완료 - 게시글 ID: {}", now, postId)
            return result
        } catch (e: Throwable) {
            // 삭제 중 예외 발생 시 에러 로그 기록
            logger.error("[{}] [DELETE ERROR] 삭제 중 에러 발생 (게시글 ID: {}): {}",
                now, postId, e.message)
            throw e
        }
    }

    /**
     * 상품 게시글 찜(like) 메소드 실행 전후에 로그를 기록합니다.
     *
     * 로그 내용:
     * - 시작 시각, 클라이언트 IP, 찜할 게시글 ID를 기록
     * - 실행 완료 시 찜 완료 로그 기록
     * - 예외 발생 시 발생한 오류 메시지 기록
     *
     * 메소드 대상: ProductPostController.likePost(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.post.post.controller.ProductPostController.likePost(..))")
    fun logLikePost(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 찜 처리할 게시글 ID는 첫 번째 인수로 전달됨
        val postId = args[0] as String
        val ip = request.remoteAddr
        // 시작 로그 기록
        logger.info("[{}] [LIKE START] 사용자IP: {}, 대상 게시글 ID: {}",
            now, ip, postId)

        try {
            val result = joinPoint.proceed()
            // 완료 후 로그에 찜 완료 메시지 기록
            logger.info("[{}] [LIKE END] 찜 완료 - 게시글 ID: {}", now, postId)
            return result
        } catch (e: Throwable) {
            // 예외 발생 시 오류 로그 기록
            logger.error("[{}] [LIKE ERROR] 찜 중 에러 발생 (게시글 ID: {}): {}",
                now, postId, e.message)
            throw e
        }
    }

    /**
     * 상품 구매(purchasePost) 메소드 실행 전후에 로그를 기록합니다.
     *
     * 로그 내용:
     * - 시작 시각, 구매를 요청한 사용자의 ID 및 클라이언트 IP, 구매 대상 게시글 ID 기록
     * - 실행 완료 시 구매 완료 로그 기록
     * - 예외 발생 시 오류 메시지와 함께 로그 기록
     *
     * 메소드 대상: ProductPostService.purchasePost(..)
     */
    @Around("execution(* com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService.purchasePost(..))")
    fun logPurchasePost(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now()
        val args = joinPoint.args
        // 구매 요청 시, 첫 번째 인자는 구매자(User) 객체, 두 번째 인자는 게시글 ID
        val buyer = args[0] as User
        val postId = args[1] as String
        val ip = request.remoteAddr
        // 구매 시작 로그: 구매자의 ID, IP, 대상 게시글 ID 등을 기록
        logger.info("[{}] [PURCHASE START] 사용자ID: {}, 사용자IP: {}, 구매 대상 게시글 ID: {}",
            now, buyer.id, ip, postId)

        try {
            val result = joinPoint.proceed()
            // 실행 후 성공 로그 기록: 구매 완료 메시지와 게시글 ID 출력
            logger.info("[{}] [PURCHASE END] 구매 완료 - 게시글 ID: {}", now, postId)
            return result
        } catch (e: Throwable) {
            // 구매 중 예외 발생 시 로그에 에러 메시지 기록
            logger.error("[{}] [PURCHASE ERROR] 구매 중 에러 발생 (게시글 ID: {}): {}",
                now, postId, e.message)
            throw e
        }
    }
}
