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

    public ProductPost write(User actor, ProductPostWriteForm body) {
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

        return productPost;
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

    public ProductPost getPost(String id) {
        return productPostRepository.findById(id).orElseThrow(
                () -> new ServiceException("404", "해당 글은 존재하지 않습니다.")
        );
    }

    public void delete(ProductPost post) {
        productPostRepository.delete(post);
    }

    @Transactional
    public void modify(ProductPost post, ProductPostModifyForm body) {
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


    }
}
