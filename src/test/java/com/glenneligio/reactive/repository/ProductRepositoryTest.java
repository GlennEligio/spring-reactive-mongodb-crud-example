package com.glenneligio.reactive.repository;

import com.glenneligio.reactive.entity.Product;
import com.glenneligio.reactive.repo.ProductRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    private Product p1, p2, p3;

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
                .expectNextMatches(p -> p.getPrice() > 18 && p.getPrice() < 20)
                .expectComplete();
    }
}
