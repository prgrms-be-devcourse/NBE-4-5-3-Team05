package com.NBE_4_5_2.Team5.domain.payment.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime
import com.NBE_4_5_2.Team5.domain.payment.enums.PaymentStatus
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import jakarta.persistence.*
import java.util.*

@Entity
class Payment(
    @Id
    @Column(updatable = false, nullable = false)
    private var _id: String = "payment-" + UUID.randomUUID(),
    private var _paymentKey: String? = null,
    @ManyToOne
    private var _buyer: User,
    private var _totalPrice: Int = 0,
    @Enumerated(EnumType.STRING)
    private var _status: PaymentStatus = PaymentStatus.IN_PROGRESS
) : BaseTime() {

    constructor(id: String, user: User, totalAmount: Int) : this(
        _id = id,
        _buyer = user,
        _totalPrice = totalAmount
    )
    constructor(user: User, totalPrice: Int, status: PaymentStatus) : this(
        _buyer = user,
        _totalPrice = totalPrice,
        _status = status
    )

    val id: String
        get() = _id

    val paymentKey: String?
        get() = _paymentKey


    val buyer: User
        get() = _buyer


    val totalPrice: Int
        get() = _totalPrice


    val status: PaymentStatus?
        get() = _status

    /**
     * 결제 상태를 업데이트합니다.
     * @param status [PaymentStatus] 결제 상태 enum
     */
    fun updateState(status: PaymentStatus) {
        this._status = status
    }

    /**
     * 결제 요청 전에 저장한 payment 메타데이터([PaymentMetaData][com.NBE_4_5_2.Team5.domain.payment.dto.PaymentMetaData]
     * )의 `totalAmount`와 인자로 들어오는 `amount`가 같은지 비교합니다.
     * @param amount 결제 요청시 클라이언트가 전달한 총 지불 금액
     * @return 요청 전에 저장한 데이터와 인자로 받은 총 가격이 동일하다면 `true`, 그렇지 않다면 `false`를 반환합니다.
     */
    fun checkValid(amount:  Int): Boolean {
        return totalPrice == amount
    }

    fun updatePaymentKey(paymentKey:  String?): Payment {
        this._paymentKey = paymentKey
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (o !is Payment) return false
        return id == o.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}
