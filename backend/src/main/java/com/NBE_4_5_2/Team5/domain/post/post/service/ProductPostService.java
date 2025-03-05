package com.NBE_4_5_2.Team5.domain.post.post.service;

import com.NBE_4_5_2.Team5.domain.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.category.repository.CategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostModifyForm;
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostWriteForm;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductCategory;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.global.dto.PageDto;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPostService {
    private final ProductPostRepository productPostRepository;
    //    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    public ProductPostResponse write(User actor, ProductPostWriteForm body) {
        String imageUrlStr = String.join(",", body.imageUrlList());

        // 글 작성
        ProductPost productPost = ProductPost.create(
                actor,
                body.productName(),
                body.productPrice(),
                body.title(),
                body.content(),
                imageUrlStr,
                body.latitude(),
                body.longitude()
        );
        productPostRepository.save(productPost);

        // 상품글에 카테고리 체크 및 추가
        List<Long> reqCategoryIdList = body.categoryIds();
        List<Category> realCategoryList = categoryRepository.findAllById(reqCategoryIdList);
        if (realCategoryList.size() != reqCategoryIdList.size()) {
            throw new ServiceException("400", "존재하지 않는 카테고리가 포함되어있습니다.");
        }
        productPost.addCategories(realCategoryList);

        productPostRepository.save(productPost);

        return ProductPostResponse.fromEntity(productPost);
    }


    public PageDto<ProductPostResponse> getPosts(int page, int pageSize, String keyword, String sort) {
        Sort.Direction sortDirection = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, pageSize,
                Sort.by(sortDirection, "createdAt"));

        Page<ProductPost> postpage;
        String likeKeyword = "%" + keyword + "%";

        if (keyword.isBlank()) {
            postpage = productPostRepository.findAllWithCategories(pageable);
        } else {
            postpage = productPostRepository.findByTitleLike(likeKeyword, pageable);
        }

        Page<ProductPostResponse> mappedPosts = postpage.map(ProductPostResponse::fromEntity);

        return new PageDto<>(mappedPosts);
    }

    public PageDto<ProductPostResponse> getMyPosts(User actor, int page, int pageSize, String sort) {
        Sort.Direction sortDirection = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, pageSize,
                Sort.by(sortDirection, "createdAt"));

        Page<ProductPost> postPage = productPostRepository.findByWriter(actor, pageable);

        Page<ProductPostResponse> mappedMyPosts = postPage.map(ProductPostResponse::fromEntity);

        return new PageDto<>(mappedMyPosts);
    }

    public ProductPostResponse getPost(String id) {
        ProductPost post = productPostRepository.findById(id).orElseThrow(
                () -> new ServiceException("404", "해당 글은 존재하지 않습니다.")
        );

        return ProductPostResponse.fromEntity(post);
    }

    public void delete(User actor, String postId) {
        ProductPost post = productPostRepository.findById(postId).orElseThrow(
                () -> new ServiceException("404", "해당 글은 존재하지 않습니다.")
        );

        if (!post.canDelete(actor)) {
            throw new ServiceException("401", "삭제 권한이 없습니다.");
        }

        productPostRepository.delete(post);
    }

    @Transactional
    public ProductPostResponse modify(User actor, String postId, ProductPostModifyForm body) {
        ProductPost post = productPostRepository.findById(postId).orElseThrow(
                () -> new ServiceException("404", "해당 글은 존재하지 않습니다.")
        );

        if (!post.canModify(actor)) {
            throw new ServiceException("401", "수정 권한이 없습니다.");
        }

        if (body.productName() != null) {
            post.setProductName(body.productName());
        }
        if (body.productPrice() != null) {
            post.setProductPrice(body.productPrice());
        }
        if (body.title() != null) {
            post.setTitle(body.title());
        }
        if (body.content() != null) {
            post.setContent(body.content());
        }
        if (body.imageUrlList() != null && !body.imageUrlList().isEmpty()) {
            post.setImage_urls(String.join(",", body.imageUrlList()));
        }
        if (body.latitude() != null) {
            post.setLatitude(body.latitude());
        }
        if (body.longitude() != null) {
            post.setLongitude(body.longitude());
        }

        if (body.categoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(body.categoryIds());
            post.getProductCategories().clear(); // 기존 카테고리 삭제

            List<ProductCategory> newProductCategories = categories.stream()
                    .map(category -> ProductCategory.builder()
                            .productPost(post)
                            .category(category)
                            .build())
                    .toList();

            post.getProductCategories().addAll(newProductCategories);
        }

        return ProductPostResponse.fromEntity(post);
    }


}
