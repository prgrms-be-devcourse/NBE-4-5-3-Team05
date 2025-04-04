package com.NBE_4_5_2.Team5.domain.user.user.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime
import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import com.NBE_4_5_2.Team5.global.exception.payment.InsufficientPayMoneyException
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

@Entity
@Table(name = "member")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @Column(updatable = false, nullable = false)
    var id: String = "user-" + UUID.randomUUID(),

    @Column(length = 20, nullable = false, unique = true)
    var username: String,

    @Column(length = 255, nullable = false)
    var password: String,

    @Column(length = 50, nullable = false, unique = true)
    var email: String,

    @Column(length = 50, nullable = false, unique = true)
    var nickname: String,

    @Column(length = 255)
    var address: String? = null,

    @Column(name = "profile_url", length = 255)
    var profileUrl: String? = null,

    var cash: Int = 0,

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var role: Role = Role.USER,

    @Column(nullable = false)
    var blocked: Boolean = false,

    @Column(name = "blocked_count", nullable = false)
    var blockedCount: Int = 0,

    @OneToMany(mappedBy = "buyer", cascade = [CascadeType.REMOVE])
    var purchasedProducts: MutableList<ProductPost> = mutableListOf(),

    @OneToMany(mappedBy = "writer", cascade = [CascadeType.REMOVE])
    var writtenProducts: MutableList<ProductPost> = mutableListOf(),

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var wroteComments: MutableList<Comment> = mutableListOf()
) : BaseTime() {

    val isAdmin: Boolean
        get() = role == Role.USER.not() // or (role == Role.ADMIN) depending on your Role implementation

    fun ban() {
        blocked = true
        blockedCount++
    }

    fun unBan() {
        if (!blocked) return
        blocked = false
    }

    /**
     * [cash]에 [totalAmount] 만큼 추가합니다.
     */
    fun chargeCash(totalAmount: Int) {
        cash += totalAmount
    }

    fun buy(product: ProductPost, amount: Int) {
        pay(amount)
        addToPurchasedProductList(product)
        product.updateStatus(ProductStatus.PURCHASED)
        product.buyer = this
    }

    private fun addToPurchasedProductList(product: ProductPost) {
        purchasedProducts.add(product)
    }

    private fun pay(amount: Int) {
        cash -= amount
    }

    fun canBuy(product: ProductPost, amount: Int) {
        if (!hasEnoughPayMoney(amount)) {
            throw InsufficientPayMoneyException("잔액이 부족합니다.")
        }
        check(product.isAvailable) { "판매중인 상품이 아닙니다." }
    }

    fun addWroteComments(comment: Comment) {
        wroteComments.add(comment)
    }

    private fun hasEnoughPayMoney(amount: Int): Boolean = cash >= amount

    fun update(nickname: String) {
        this.nickname = nickname
    }

    val authorities: Collection<GrantedAuthority>
        get() = memberAuthoritiesAsString.map { SimpleGrantedAuthority(it) }

    val memberAuthoritiesAsString: List<String>
        get() = buildList {
            if (isAdmin) add("ROLE_ADMIN")
        }

    fun addWrittenPost(saved: ProductPost) {
        purchasedProducts.add(saved)
    }

    fun setAdmin() {
        role = Role.ADMIN
    }
}
