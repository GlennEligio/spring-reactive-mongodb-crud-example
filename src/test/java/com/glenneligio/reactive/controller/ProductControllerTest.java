package com.glenneligio.reactive.controller;

import com.glenneligio.reactive.dto.ProductDto;
import com.glenneligio.reactive.entity.Product;
import com.glenneligio.reactive.service.ProductService;
import com.glenneligio.reactive.util.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProductController.class)
@Slf4j
public class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService service;

    private Product p1, p2, p3;

    @BeforeEach
    void setup() {
        p1 = new Product(new ObjectId().toHexString(), "Product1", 1, 20.1);
        p2 = new Product(new ObjectId().toHexString(), "Product2", 2, 25);
        p3 = new Product(new ObjectId().toHexString(), "Product3", 2, 30);
    }

    @Test
    @DisplayName("Get all products and returns 200OK with the Products")
    void getProducts_returns200OKWithProducts() {
        Flux<Product> productFlux = Flux.just(p1, p2, p3);
        when(service.getProducts()).thenReturn(productFlux);

        Flux<ProductDto> productFluxExchangeResult = webTestClient.get()
                .uri("/products")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductDto.class)
                .getResponseBody();

        StepVerifier.create(productFluxExchangeResult)
                .expectSubscription()
                .expectNext(AppUtils.entityToDto(p1))
                .expectNext(AppUtils.entityToDto(p2))
                .expectNext(AppUtils.entityToDto(p3))
                .verifyComplete();
    }

    @Test
    @DisplayName("Get product with valid id returns 200OK with corresponding Product")
    void getProductById_withValidId_returns200OKWithCorrectProduct() {
        Mono<Product> productMono = Mono.just(p1);
        String validId = p1.getId();
        when(service.getProductById(validId)).thenReturn(productMono);

        Mono<ProductDto> productMonoResult = webTestClient.get()
                .uri("/products/" + validId)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductDto.class)
                .getResponseBody().single();

        StepVerifier.create(productMonoResult)
                .expectSubscription()
                .expectNext(AppUtils.entityToDto(p1))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Get products within a price range with min and max returns 200OK with Products within the range")
    void getProductsByPriceRange_withValidMinAndMaxReqParam_with200OKWithCorrectProducts() {
        double min = 20;
        double max = 26;
        Flux<Product> productFlux = Flux.just(p1, p2, p3).filter(p -> p.getPrice() > min && p.getPrice() < max);
        when(service.getProductByPriceRange(min, max)).thenReturn(productFlux);

        Flux<ProductDto> productFluxResult = webTestClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/products/range")
                            .queryParam("min", min)
                            .queryParam("max", max).build();
                }).exchange()
                .expectStatus().isOk()
                .returnResult(ProductDto.class)
                .getResponseBody();

        StepVerifier.create(productFluxResult)
                .expectSubscription()
                .expectNextMatches(p -> p.getPrice() > min && p.getPrice() < max)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Create Product and returns the Product created")
    void saveProduct_withProperPayload_returns201CreatedWithNewProduct() {
        ProductDto dtoRequest = AppUtils.entityToDto(p1);
        Product productToBeSaved = AppUtils.dtoToEntity(dtoRequest);
        log.info("Product to be saved {}", productToBeSaved);
        Mono<Product> savedProductMono = Mono.just(p1);
        when(service.saveProduct(productToBeSaved)).thenReturn(savedProductMono);

        Mono<ProductDto> dtoResponse = webTestClient.post()
                .uri("/products")
                .bodyValue(AppUtils.entityToDto(p1))
                .exchange()
                .expectStatus().isOk() // TODO: Configure the endpoint so that Response is 201 Created
                .returnResult(ProductDto.class)
                .getResponseBody().single();

        StepVerifier.create(dtoResponse)
                .expectSubscription()
                .expectNext(AppUtils.entityToDto(p1))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Update Product and returns 200 OK with updated Product")
    void updateProduct_withProperPayload_returns200OkWithUpdatedProduct() {
        ProductDto dtoRequest = AppUtils.entityToDto(p1);
        String validId = p1.getId();
        when(service.updateProduct(AppUtils.dtoToEntity(dtoRequest), validId)).thenReturn(Mono.just(p1));

        Mono<ProductDto> dtoResponse = webTestClient.put()
                .uri("/products/" + validId)
                .body(Mono.just(dtoRequest), ProductDto.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductDto.class)
                .getResponseBody().single();

        StepVerifier.create(dtoResponse)
                .expectSubscription()
                .expectNext(dtoRequest)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Delete Product using valid Id returns 200OK")
    void deleteProduct_withValidId_returns200OK() {
        String validId = p1.getId();
        when(service.deleteProduct(validId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/products/" + validId)
                .exchange()
                .expectStatus().isOk();
    }
}
