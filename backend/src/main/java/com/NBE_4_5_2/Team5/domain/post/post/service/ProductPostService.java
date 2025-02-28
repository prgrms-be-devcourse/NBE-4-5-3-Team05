package com.NBE_4_5_2.Team5.domain.post.post.service;

import com.NBE_4_5_2.Team5.domain.post.post.dto.WriteReqBody;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.global.dto.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductPostService {
    private final ProductPostRepository productPostRepository;
//    private final MemberRepository memberRepository;

    public ProductPost write(WriteReqBody body) {
        String imageUrlStr = String.join(",", body.imageUrlList());

        ProductPost productPost = ProductPost.create(
                body.productName(),
                body.productPrice(),
                body.title(),
                body.content(),
//                writer,
                imageUrlStr,
                body.latitude(),
                body.longitude()
        );

        return productPostRepository.save(productPost);
    }


    public PageDto<ProductPost> getPosts(int page, int pageSize, String keyword, String sort) {
        Pageable pageable = PageRequest.of(page - 1, pageSize,
                Sort.by(sort.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, "id"));
        String likeKeyword = "%" + keyword + "%";

        // keyword가 제목에 들어간 게시물 목록
        Page<ProductPost> mappedPosts = productPostRepository.findByTitleLike(likeKeyword, pageable);

        return new PageDto<>(mappedPosts);
    }
}
