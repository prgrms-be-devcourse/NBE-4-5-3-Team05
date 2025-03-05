package com.NBE_4_5_2.Team5.domain.post.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.NBE_4_5_2.Team5.domain.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.comment.entity.Comment;
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;

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
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ProductPost {

	@Id
	@Column(updatable = false, nullable = false)
	private String id;

	@Column(nullable = false)
	private String productName;

	@Column(nullable = false)
	private Integer productPrice;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String imageUrls; // 쉼표가 포함된 url 문자열

	@Column(nullable = false)
	@Builder.Default
	private Integer likedCount = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ProductStatus status = ProductStatus.AVAILABLE;

	@Column(nullable = false)
	private Float latitude;

	@Column(nullable = false)
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
		String content, String imageUrls, Float latitude, Float longitude) {
		return ProductPost.builder()
			.id("ppost-" + UUID.randomUUID())
			.writer(writer)
			.productName(productName)
			.productPrice(productPrice)
			.title(title)
			.content(content)
			.imageUrls(imageUrls)
			.latitude(latitude)
			.longitude(longitude)
			.build();
	}

	public void addCategories(List<Category> categories) {
		List<ProductCategory> productCategories = categories.stream()
			.map(category -> ProductCategory.builder().productPost(this).category(category).build())
			.toList();

		this.productCategories.addAll(productCategories);
	}

	public boolean canModify(User writer) {
		if (writer == null) {
			throw new ServiceException("401-1", "인증 정보가 없습니다.");
		}

		if (writer.isAdmin() || writer.equals(this.getWriter())) {
			return true;
		}

		throw new ServiceException("403-1", "자신이 작성한 글만 수정 가능합니다.");
	}

	public boolean canDelete(User writer) {
		if (writer == null) {
			throw new ServiceException("401-1", "인증 정보가 없습니다.");
		}

		if (writer.isAdmin() || writer.equals(this.getWriter())) {
			return true;
		}

		throw new ServiceException("403-1", "자신이 작성한 글만 삭제 가능합니다.");
	}

	public void addComment(Comment comment) {
		commentList.add(comment);
	}
}
