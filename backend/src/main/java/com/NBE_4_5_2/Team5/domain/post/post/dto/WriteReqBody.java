package com.NBE_4_5_2.Team5.domain.post.post.dto;

import java.util.List;

public record WriteReqBody(
        String productName,
        Integer productPrice,
        String title,
        String content,
        List<String> imageUrlList,
        Float latitude,
        Float longitude
) {}
