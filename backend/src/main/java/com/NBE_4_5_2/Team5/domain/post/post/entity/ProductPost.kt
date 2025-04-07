package com.NBE_4_5_2.Team5.domain.post.post.entity

import com.NBE_4_5_2.Team5.domain.base.entity.BaseTime
import com.NBE_4_5_2.Team5.domain.post.category.entity.Category
import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotFoundException
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
class ProductPost() : BaseTime() {

    @Id
    @Column(updatable = false, nullable = false)
    val id: String = "ppost-" + UUID.randomUUID()

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var writer: User

    @Column(nullable = false)
    lateinit var productName: String

    @Column(nullable = false)
    var productPrice: Int = 0

    @Column(nullable = false)
    lateinit var title: String

    @Column(nullable = false, columnDefinition = "TEXT")
    lateinit var content: String

    @Column(name = "image_urls", nullable = false, columnDefinition = "TEXT")
    lateinit var imageUrls: String

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var buyer: User

    @Column(nullable = false)
    var viewCount: Int = 0

    @Column(nullable = false)
    var likeCount: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProductStatus = ProductStatus.AVAILABLE

    @Column(nullable = false)
    var latitude: Float = 0f

    @Column(nullable = false)
    var longitude: Float = 0f

    @OneToMany(mappedBy = "productPost", cascade = [CascadeType.ALL], orphanRemoval = true)
    val productCategories: MutableList<ProductCategory> = mutableListOf()

    @OneToMany(mappedBy = "target", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val commentList: MutableList<Comment> = mutableListOf()

    constructor(
        writer: User,
        productName: String,
        productPrice: Int,
        title: String,
        content: String,
        imageUrls: String,
        latitude: Float,
        longitude: Float
    ) : this() {
        this.writer = writer
        this.productName = productName
        this.productPrice = productPrice
        this.title = title
        this.content = content
        this.imageUrls = imageUrls
        this.latitude = latitude
        this.longitude = longitude
        setCreateDateNow();
    }

    fun addCategories(categories: List<Category>) {
        val newCategories = categories.map {
            ProductCategory.of(productPost = this, category = it)
        }
        productCategories.addAll(newCategories)
    }

    fun purchaseBy(buyer: User) {
        this.buyer = buyer
        this.status = ProductStatus.PURCHASED
    }

    fun canModify(writer: User?) {
        when {
            writer == null -> throw AuthenticationNotFoundException("401-1", "인증 정보가 없습니다.")
            writer.isAdmin() || writer == this.writer -> return
            else -> throw ForbiddenAccessException("403-1", "자신이 작성한 글만 수정 가능합니다.")
        }
    }

    fun canDelete(writer: User?) {
        when {
            writer == null -> throw AuthenticationNotFoundException("401-1", "인증 정보가 없습니다.")
            writer.isAdmin() || writer == this.writer -> return
            else -> throw ForbiddenAccessException("403-1", "자신이 작성한 글만 삭제 가능합니다.")
        }
    }

    fun isAvailable(): Boolean = status == ProductStatus.AVAILABLE

    fun updateStatus(status: ProductStatus) {
        this.status = status
    }

    fun addComment(comment: Comment) {
        commentList.add(comment)
    }

    fun incrementViewCount() {
        viewCount++
    }

    fun isPurchasedBy(user: User): Boolean {
        return buyer == user
    }
}