package com.NBE_4_5_2.Team5.domain.post.post.service;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostWriteForm;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.PreviewPostResponse;
import com.NBE_4_5_2.Team5.domain.post.post.dto.response.ProductPostResponse;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.NBE_4_5_2.Team5.global.dto.PageDto;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@BaseTestConfig
public class ProductPostServiceTest {

    @Autowired
    private ProductPostService productPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;


    private User testUser;
    private List<Long> categoryIds;

    @BeforeEach
    void setUp() {
        testUser = userService.getUserByUsername("user1").orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않습니다")
        );
        categoryIds = categoryRepository.findAll().stream()
                .map(Category::getId)
                .limit(1)
                .toList();
    }

    @Test
    void write() {
        ProductPostWriteForm form = new ProductPostWriteForm(
                "테스트 상품",
                12345,
                "테스트 제목",
                "테스트 내용입니다.",
                categoryIds,
                List.of("http://localhost/images/test.jpg"),
                37.5f,
                127.0f
        );

        ProductPostResponse response = productPostService.write(testUser, form);

        assertThat(response.getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    void getPosts() {
        PageDto<PreviewPostResponse> posts = productPostService.getPosts(1, 10, "", "desc");
        List<PreviewPostResponse> items = posts.getItems();

        assertThat(items.size()).isEqualTo(10);
    }

    @Test
    void getPost() {
        PreviewPostResponse res = productPostService.getPosts(1, 1, "", "desc").getItems().get(0);
        ProductPostResponse post = productPostService.getPost(res.getId());
        assertThat(res.getId()).isEqualTo(post.getId());
    }
}
