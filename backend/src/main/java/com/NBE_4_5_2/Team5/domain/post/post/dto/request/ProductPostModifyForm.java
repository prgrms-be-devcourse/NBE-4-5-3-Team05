package com.NBE_4_5_2.Team5.domain.post.post.dto.request;

import java.util.List;

public record ProductPostModifyForm(
        String productName,
        Integer productPrice,
        String title,
        String content,
        List<Long> categoryIds,
        List<String> imageUrlList,
        Float latitude,
        Float longitude
) {}
