package com.glenneligio.reactive.service;

import com.glenneligio.reactive.entity.Product;
import com.glenneligio.reactive.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public Flux<Product> getProducts() {
        log.info("Fetching products in database");
        return repository.findAll();
    }

    public Mono<Product> getProductById(String id) {
        log.info("Fetching product in database with id {}", id);
        return repository.findById(id);
    }

    public Flux<Product> getProductByPriceRange(double min, double max) {
        log.info("Fetching all products with price between {} and {} in database", min, max);
        return repository.findByPriceBetween(Range.closed(min, max));
    }

    public Mono<Product> saveProduct(Product product) {
        log.info("Saving product {}", product);
        return repository.save(product);
    }

    public Mono<Product> updateProduct(Product product, String id) {
        log.info("Updating product with id {}, using data {}", id, product);
        return repository.findById(id)
                .flatMap(p -> Mono.just(product))
                .doOnNext(p -> p.setId(id))
                .flatMap(repository::save);
    }

    public Mono<Void> deleteProduct(String id) {
        log.info("Deleting product with id {}", id);
        return repository.deleteById(id);
    }
}
