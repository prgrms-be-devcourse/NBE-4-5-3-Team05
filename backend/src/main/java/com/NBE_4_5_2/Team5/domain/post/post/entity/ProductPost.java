package com.NBE_4_5_2.Team5.domain.post.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment;
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.global.exception.security.AuthenticationNotFoundException;
import com.NBE_4_5_2.Team5.global.exception.security.ForbiddenAccessException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductPost {

	@Id
	@Column(updatable = false, nullable = false)
	private String id;

	@Column(nullable = false)
	@Setter
	private String productName;

	@Column(nullable = false)
	@Setter
	private Integer productPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	private User buyer; // 구매자(NULL이면 아직 구매 안 된 상태)

	@Column(nullable = false)
	@Setter
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	@Setter
	private String content;

	@Column(nullable = false, columnDefinition = "TEXT")
	@Setter
	private String image_urls; // 쉼표가 포함된 url 문자열

	//조회수
	@Column(nullable = false)
	@Builder.Default
	private Integer viewCount = 0;

	@Builder.Default
	@Column(nullable = false)
	private Integer likeCount = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Setter
	@Builder.Default
	private ProductStatus status = ProductStatus.AVAILABLE;

	@Column(nullable = false)
	@Setter
	private Float latitude;

	@Column(nullable = false)
	@Setter
	private Float longitude;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime modifiedAt;

	@OneToMany(mappedBy = "productPost", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ProductCategory> productCategories = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	private User writer;

	@OneToMany(mappedBy = "target", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@Builder.Default
	private final List<Comment> commentList = new ArrayList<>();

	public static ProductPost create(User writer, String productName, Integer productPrice, String title,
		String content, String image_urls, Float latitude, Float longitude) {
		return ProductPost.builder()
			.id("ppost-" + UUID.randomUUID())
			.writer(writer)
			.productName(productName)
			.productPrice(productPrice)
			.title(title)
			.content(content)
			.image_urls(image_urls)
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}

	// 구매 처리 메서드
	public void setBuyer(User buyer) {
		this.buyer = buyer;
		this.status = ProductStatus.PURCHASED;
	}

	public void addCategories(List<Category> categories) {
		List<ProductCategory> productCategories = categories.stream()
			.map(category -> ProductCategory.builder()
				.productPost(this)
				.category(category)
				.build())
			.toList();

		this.productCategories.addAll(productCategories);
	}

	public void canModify(User writer) {
		if (writer == null) {
			throw new AuthenticationNotFoundException("401-1", "인증 정보가 없습니다.");
		}

		if (writer.isAdmin() || writer.equals(this.getWriter())) {
			return;
		}

		throw new ForbiddenAccessException("403-1", "자신이 작성한 글만 수정 가능합니다.");
	}

	public void canDelete(User writer) {
		if (writer == null) {
			throw new AuthenticationNotFoundException("401-1", "인증 정보가 없습니다.");
		}

		if (writer.isAdmin() || writer.equals(this.getWriter())) {
			return;
		}

		throw new ForbiddenAccessException("403-1", "자신이 작성한 글만 삭제 가능합니다.");
	}

	public boolean isAvailable() {
		return status == ProductStatus.AVAILABLE;
	}

	public void updateStatus(ProductStatus status) {
		this.status = status;
	}

	public void addComment(Comment comment) {
		commentList.add(comment);
	}

	//조회수 증가
	public void incrementViewCount() {
		this.viewCount++;
	}

	public Boolean isPurchasedBy(User loggedInUser) {
		return buyer.equals(loggedInUser);
	}
}