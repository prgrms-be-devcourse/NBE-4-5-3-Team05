package com.NBE_4_5_2.Team5.domain.post.post.controller;

import com.NBE_4_5_2.Team5.domain.post.post.dto.WriteReqBody;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.global.dto.PageDto;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.Reader;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ProductPostController {
    private final ProductPostService productPostService;

    @PostMapping
    public RsData<ProductPost> createPost(@Valid @RequestBody WriteReqBody body) {

        // 작성자 체크 및 write에 넘겨주기 추가 필요
        // 나중에 DTO 리턴 가능
        ProductPost post = productPostService.write(body);

        return new RsData<>(
                "200",
                "글 작성 성공",
                post
        );
    }

    @GetMapping
    @Transactional(readOnly = true)
    public RsData<PageDto<ProductPost>> getPosts(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(defaultValue = "keyword") String keyword,
                                                 @RequestParam(defaultValue = "asc") String sort, Reader reader) {
        PageDto<ProductPost> postPage = productPostService.getPosts(page,pageSize,keyword,sort);

    }
//
//    @GetMapping("/{id}")
//    public void getPost() {
//
//    }
//
//    @PutMapping("/{id}")
//    public void modify() {
//
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete() {
//
//    }

}
