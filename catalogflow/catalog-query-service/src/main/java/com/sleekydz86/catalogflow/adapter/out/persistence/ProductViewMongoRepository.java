package com.sleekydz86.catalogflow.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductViewMongoRepository extends MongoRepository<ProductViewDocument, String> {
}
