package com.NBE_4_5_2.Team5.domain.product.controller;

import com.NBE_4_5_2.Team5.domain.product.dto.ProductDto;
import com.NBE_4_5_2.Team5.domain.product.service.ProductService;
import com.NBE_4_5_2.Team5.global.dto.RsData;
import com.NBE_4_5_2.Team5.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    RsData<List<ProductDto>> getProduct(@RequestParam String type) {
        if (!"purchased".equals(type)) {
            throw new ServiceException("400-INVALID_PARAM", "Invalid type parameter");
        }
        List<ProductDto> products = productService.getProduct();
        return new RsData<>("200-SUCCESS", "내 구매내역 조회 성공", products);
    }

    //판매 내역 조회
    @GetMapping("/sales")
    public RsData<List<ProductDto>> getSalesHistory(@RequestParam(name = "type") String type, @RequestParam(name = "userId") String userId) {
        if (!type.equals("sold") && !type.equals("selling") && !type.equals("reserved")) {
            throw new ServiceException("400-INVALID_PARAM", "Invalid type parameter");
        }
        List<ProductDto> products = productService.getSalesHistory(userId, type);
        return new RsData<>("200-SUCCESS", "내 판매내역 조회 성공", products);
    }


}
