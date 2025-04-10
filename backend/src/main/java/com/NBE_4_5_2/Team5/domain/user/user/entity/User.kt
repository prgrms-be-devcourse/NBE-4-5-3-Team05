package com.NBE_4_5_2.Team5.domain.user.user.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime
import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import com.NBE_4_5_2.Team5.global.exception.payment.InsufficientPayMoneyException
import jakarta.persistence.*
import org.hibernate.Hibernate
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

@Entity
@Table(name = "member")
class User() : BaseTime() {

    @Id
    @Column(updatable = false, nullable = false)
    var id: String = "user-" + UUID.randomUUID()

    @Column(length = 20, nullable = false, unique = true)
    lateinit var username: String

    @Column(length = 255, nullable = false)
    lateinit var password: String

    @Column(length = 50, nullable = false, unique = true)
    lateinit var email: String

    @Column(length = 50, nullable = false, unique = true)
    lateinit var nickname: String

    @Column(length = 255)
    lateinit var address: String

    @Column(name = "profile_url", length = 255)
    lateinit var profileUrl: String

    var cash: Int = 0

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var role: Role = Role.USER;

    @Column(nullable = false)
    var blocked: Boolean = false;

    @Column(name = "blocked_count", nullable = false)
    var blockedCount: Int = 0;

    @OneToMany(mappedBy = "buyer", cascade = [CascadeType.REMOVE])
    val purchasedProducts: MutableList<ProductPost> = mutableListOf()

    @OneToMany(mappedBy = "writer", cascade = [CascadeType.REMOVE])
    val writtenProducts: MutableList<ProductPost> = mutableListOf()

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val wroteComments: MutableList<Comment> = mutableListOf()

    val isAdmin: Boolean
        get() = role == Role.ADMIN

    constructor(id: String, username: String, nickname: String, role: Role) : this() {
        this.id = id
        this.username = username
        this.nickname = nickname
        this.role = role
    }

    constructor(
        username: String, password: String, email: String, nickname: String,
        address: String, profileUrl: String, role: Role
    ) : this() {
        this.username = username
        this.password = password
        this.email = email
        this.blocked = false
        this.blockedCount = 0
        this.nickname = nickname
        this.address = address
        this.profileUrl = profileUrl
        this.role = role
    }

    fun ban() {
        this.blocked = true
        this.blockedCount++
    }

    fun unBan() {
        if (!blocked) return
        this.blocked = false
    }

    /**
     * [cash][User.cash]에 `totalAmount` 만큼 추가합니다.
     *
     * @param totalAmount 충전할 금액
     */
    fun chargeCash(totalAmount: Int) {
        this.cash += totalAmount
    }

    fun buy(product: ProductPost, amount: Int) {
        pay(amount)
        addToPurchasedProductList(product)
        product.updateStatus(ProductStatus.PURCHASED)
        product.purchaseBy(this)
    }

    //TODO : Member 객체의 구현에 따라 구매 상품을 담을 list에 업데이트 필요

    /**
     * 유저의 구매 이력에 [ProductPost]를 추가하는 메서드
     * @param product 구매 이력에 추가할 구매한 상품 객체
     */
    private fun addToPurchasedProductList(product: ProductPost) {
        purchasedProducts.add(product)
    }

    private fun pay(amount: Int) {
        cash -= amount
    }

    /**
     * [product][ProductPost]를 [amount][Integer]로 구매할 수 있는지 판단하는 메서드.
     *
     * @param product 구매할 상품 객체
     * @param amount  결제할 총 가격
     * @throws InsufficientPayMoneyException 총 결제 가격 `amount`보다 가지고 있는 잔액인 `cash`가 적을 경우 발생
     * @throws IllegalArgumentException      상품의 판매 상태가
     *                                       [ProductStatus.AVAILABLE][ProductStatus]이
     *                                       아닌 경우 발생
     */
    fun canBuy(product: ProductPost, amount: Int) {
        if (!hasEnoughPayMoney(amount)) throw InsufficientPayMoneyException("잔액이 부족합니다.")
        if (!product.isAvailable()) throw IllegalStateException("판매중인 상품이 아닙니다.")
    }

    fun addWroteComments(comment: Comment) {
        wroteComments.add(comment)
    }

    /**
     * 해당 유저가 페이머니가 충분한지 검사하는 메서드.
     *
     * @param amount 비교할 금액
     * @return [cash][User.cash]가 비교할 금액보다 많다면 `true`, 그렇지 않다면 `false`를 반환
     */
    private fun hasEnoughPayMoney(amount: Int): Boolean {
        return cash >= amount
    }

    fun update(nickname: String) {
        this.nickname = nickname
    }

    val authorities: List<GrantedAuthority>
        get() = memberAuthoritiesAsString.map { SimpleGrantedAuthority(it) }

    val memberAuthoritiesAsString: List<String>
        get() = if (isAdmin) listOf("ROLE_ADMIN") else emptyList()

    fun addWrittenPost(saved: ProductPost) {
        purchasedProducts.add(saved)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other))
            return false

        other as User
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}