package com.NBE_4_5_2.Team5.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus;
import com.NBE_4_5_2.Team5.global.entity.BaseTime;
import com.NBE_4_5_2.Team5.global.exception.payment.InsufficientPayMoneyException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
public class User extends BaseTime {

	@Column(length = 20, nullable = false, unique = true)
	private String username;

	@Column(length = 255, nullable = false)
	private String password;

	@Column(length = 100, unique = true)
	private String refreshToken;

	@Column(length = 50, nullable = false, unique = true)
	private String email;

	@Column(length = 20, nullable = false, unique = true)
	private String nickname;

	@Column(length = 255)
	private String address;

	@Column(name = "profile_url", length = 255)
	private String profileUrl;

	private int cash;

	@Column(nullable = false)
	@Builder.Default
	private Integer role = 1;  // 0: Admin, 1: 일반 유저

	@Column(nullable = false)
	@Builder.Default
	private Boolean blocked = false;

	@Column(name = "blocked_count", nullable = false)
	@Builder.Default
	private Integer blockedCount = 0;

	@OneToMany(mappedBy = "buyer", cascade = CascadeType.REMOVE)
	private final List<ProductPost> purchasedProducts = new ArrayList<>();

	public boolean isAdmin() {
		return this.role == 0;
	}

	/**
	 * {@link User#cash cash}에 {@code totalAmount} 만큼 추가합니다.
	 * @param totalAmount 충전할 금액
	 */
	public void chargeCash(Integer totalAmount) {
		this.cash += totalAmount;
	}

	public void buy(ProductPost product, Integer amount) {
		pay(amount);
		addToPurchasedProductList(product);
		product.updateStatus(ProductStatus.PURCHASED);
	}

	//TODO : Member 객체의 구현에 따라 구매 상품을 담을 list에 업데이트 필요

	/**
	 * 유저의 구매 이력에 {@link ProductPost}를 추가하는 메서드
	 * @param product 구매 이력에 추가할 구매한 상품 객체
	 */
	private void addToPurchasedProductList(ProductPost product) {
	}

	private void pay(Integer amount) {
		cash -= amount;
	}

	/**
	 * {@link ProductPost product}를 {@link Integer amount}로 구매할 수 있는지 판단하는 메서드.
	 *
	 * @throws InsufficientPayMoneyException 총 결제 가격 {@code amount}보다 가지고 있는 잔액인 {@code cash}가 적을 경우 발생
	 * @throws IllegalArgumentException 상품의 판매 상태가
	 * {@link com.NBE_4_5_2.Team5.domain.post.post.enums.ProductStatus#AVAILABLE ProductStatus.AVAILABLE}이<br/>
	 * 아닌 경우 발생
	 *
	 * @param product 구매할 상품 객체
	 * @param amount 결제할 총 가격
	 * @return 상품을 해당 유저가 구매 가능하다면 {@code true}를 반환한다.
	 */
	public boolean canBuy(ProductPost product, Integer amount) {
		if (!this.hasEnoughPayMoney(amount)) {
			throw new InsufficientPayMoneyException("잔액이 부족합니다.");
		}

		if (!product.isAvailable()) {
			throw new IllegalStateException("판매중인 상품이 아닙니다.");
		}

		return true;
	}

	/**
	 * 해당 유저가 페이머니가 충분한지 검사하는 메서드.
	 *
	 * @param amount 비교할 금액
	 * @return {@link User#cash cash}가 비교할 금액보다 많다면 {@code true}, 그렇지 않다면 {@code false}를 반환
	 **/
	private boolean hasEnoughPayMoney(Integer amount) {
		return cash >= amount;
	}
}
