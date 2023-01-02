package com.glenneligio.reactive.service;

import com.glenneligio.reactive.entity.Product;
import com.glenneligio.reactive.repo.ProductRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Range;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService service;

    @MockBean
    private ProductRepository repoMock;

    private Product p1, p2, p3;

    @BeforeEach
    void setup() {
        p1 = new Product(new ObjectId().toHexString(), "Product1", 1, 20.1);
        p2 = new Product(new ObjectId().toHexString(), "Product2", 2, 25);
        p3 = new Product(new ObjectId().toHexString(), "Product3", 2, 30);
    }

    @Test
    @DisplayName("Fetch all Products")
    void getProducts_returnAllProducts() {
        Flux<Product> expectedFlux = Flux.just(p1, p2, p3);
        Mockito.when(repoMock.findAll()).thenReturn(expectedFlux);

        Flux<Product> resultFlux = service.getProducts();

        StepVerifier.create(resultFlux)
                .expectSubscription()
                .expectNext(p1, p2, p3)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Fetch product by id")
    void getProductById_returnTheMatchingProduct() {
        Mono<Product> productMonoExpected = Mono.just(p1);
        Mockito.when(repoMock.findById(p1.getId())).thenReturn(productMonoExpected);

        Mono<Product> productMonoResult = service.getProductById(p1.getId());

        StepVerifier.create(productMonoResult)
                .expectSubscription()
                .expectNext(p1)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Fetching products by price range")
    void getProductByPriceRange_returnsProductsWhoPriceIsWithinRange() {
        double min = 20.0;
        double max = 26.0;
        Flux<Product> productFluxExpected = Flux.just(p1, p2, p3)
                .filter(p -> p.getPrice() > min && p.getPrice() < max);
        Mockito.when(repoMock.findByPriceBetween(Range.closed(min, max))).thenReturn(productFluxExpected);

        Flux<Product> productFluxResult = service.getProductByPriceRange(min, max);

        StepVerifier.create(productFluxResult)
                .expectSubscription()
                .expectNext(p1, p2)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Save product")
    void saveProduct_returnsNewProduct() {
        Mono<Product> productMonoExpected = Mono.just(p1);
        Mockito.when(repoMock.save(p1)).thenReturn(productMonoExpected);

        Mono<Product> productMonoResult = service.saveProduct(p1);

        StepVerifier.create(productMonoResult)
                .expectSubscription()
                .expectNext(p1)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Update product")
    void updateProduct_returnsUpdatedProduct() {
        Product updatedProduct = new Product(p1.getId(), "UpdatedName1", 79, 420.0);
        Mono<Product> productMonoExpected = Mono.just(updatedProduct);
        Mockito.when(repoMock.findById(p1.getId())).thenReturn(Mono.just(p1));
        Mockito.when(repoMock.save(updatedProduct)).thenReturn(productMonoExpected);

        Mono<Product> productMonoResult = service.updateProduct(updatedProduct, p1.getId());

        StepVerifier.create(productMonoResult)
                .expectSubscription()
                .expectNext(updatedProduct)
                .verifyComplete();
    }

    @Test
    @DisplayName("Delete product")
    void deleteProduct_returnsMonoOfTypeVoid() {
        String validId = p1.getId();
        Mockito.when(service.deleteProduct(validId)).thenReturn(Mono.empty());

        Mono<Void> monoResult = service.deleteProduct(validId);

        StepVerifier.create(monoResult)
                .expectSubscription()
                .verifyComplete();
    }
}
