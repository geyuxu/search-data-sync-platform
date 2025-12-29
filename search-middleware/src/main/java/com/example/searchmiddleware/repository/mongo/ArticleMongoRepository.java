package com.example.searchmiddleware.repository.mongo;

import com.example.searchmiddleware.model.Article;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleMongoRepository extends MongoRepository<Article, String> {
}
