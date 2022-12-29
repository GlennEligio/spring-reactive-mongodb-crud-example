package com.glenneligio.reactive.controller;

import com.glenneligio.reactive.Application;
import com.glenneligio.reactive.dto.ProductDto;
import com.glenneligio.reactive.entity.Product;
import com.glenneligio.reactive.service.ProductService;
import com.glenneligio.reactive.util.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping
    public Flux<ProductDto> getProducts() {
        log.info("Getting all the products");
       return service.getProducts().map(AppUtils::entityToDto);
    }

    @GetMapping("/{id}")
    public Mono<ProductDto> getProductById(@PathVariable String id) {
        log.info("Getting product with id {}", id);
        return service.getProductById(id)
                .map(AppUtils::entityToDto);
    }

    @GetMapping("/range")
    public Flux<ProductDto> getProductByPriceRange(@RequestParam("min") double min,
                                                   @RequestParam("max") double max){
        log.info("Getting all products with price between {} and {}", min, max);
        return service.getProductByPriceRange(min, max)
                .map(AppUtils::entityToDto);
    }

    @PostMapping
    public Mono<ProductDto> saveProduct(@RequestBody Mono<ProductDto> monoDto) {
        monoDto.log().subscribe();
        Mono<Product> savedProduct = service.saveProduct(monoDto.map(AppUtils::dtoToEntity))
                .log();
        return savedProduct.map(AppUtils::entityToDto);
    }

    @PutMapping("/{id}")
    public Mono<ProductDto> updateProduct(@RequestBody Mono<ProductDto> monoDto,
                                          @PathVariable String id) {
        log.info("Updating product with id {} using data {}", id, monoDto);
        return service.updateProduct(monoDto.map(AppUtils::dtoToEntity), id)
                .map(AppUtils::entityToDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteProduct(@PathVariable("id") String id) {
        log.info("Deleting product with id {}", id);
        return service.deleteProduct(id);
    }
}
