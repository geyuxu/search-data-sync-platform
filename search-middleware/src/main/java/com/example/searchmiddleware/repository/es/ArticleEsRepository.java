package com.example.searchmiddleware.repository.es;

import com.example.searchmiddleware.model.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleEsRepository extends ElasticsearchRepository<Article, String> {
}
