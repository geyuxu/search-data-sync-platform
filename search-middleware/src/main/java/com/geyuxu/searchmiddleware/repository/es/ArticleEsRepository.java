package com.geyuxu.searchmiddleware.repository.es;

import com.geyuxu.searchmiddleware.model.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleEsRepository extends ElasticsearchRepository<Article, String> {
}
