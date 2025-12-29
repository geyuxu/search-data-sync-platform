package com.geyuxu.searchmiddleware.repository.jpa;

import com.geyuxu.searchmiddleware.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {
}
