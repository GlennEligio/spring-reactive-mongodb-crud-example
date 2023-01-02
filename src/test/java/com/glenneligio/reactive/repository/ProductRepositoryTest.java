package com.glenneligio.reactive.repository;

import com.glenneligio.reactive.entity.Product;
import com.glenneligio.reactive.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Range;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataMongoTest
@Testcontainers
@Slf4j
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Container
    public static MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:4.4.3"));

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    private Product p1, p2, p3;

    @BeforeAll
    static void initAll() {
        container.start();
    }

    @BeforeEach
    void setup() {
        p1 = new Product(new ObjectId().toHexString(), "Product1", 1, 20.1);
        p2 = new Product(new ObjectId().toHexString(), "Product2", 2, 25);
        p3 = new Product(new ObjectId().toHexString(), "Product3", 2, 30);
    }

    @Test
    @DisplayName("Fetching all Products between the price range")
    void findByPriceBetween_returnsProductsWhosePriceIsInRange() {
        repository.save(p1).block();
        repository.save(p2).block();
        repository.save(p3).block();

        double min = 20;
        double max = 26;

        Flux<Product> resultProductFlux = repository.findByPriceBetween(Range.closed(min, max));

        StepVerifier.create(resultProductFlux)
                .expectSubscription()
                .expectNext(p1)
                .expectNext(p2)
                .expectComplete()
                .verify();
    }
}
