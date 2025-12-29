package com.example.searchmiddleware.controller;

import com.example.searchmiddleware.model.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import com.example.searchmiddleware.repository.es.ArticleEsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class SearchController {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ArticleEsRepository articleEsRepository;

    @GetMapping("/recent")
    public List<Article> getRecent(@RequestParam(defaultValue = "10") int limit) {
        return articleEsRepository.findAll(PageRequest.of(0, limit)).getContent();
    }

    @GetMapping("/search")
    public List<Article> search(@RequestParam String q) {
        Criteria criteria = new Criteria("title").contains(q)
                .or(new Criteria("summary").contains(q));

        Query query = new CriteriaQuery(criteria);

        // Add highlighting
        HighlightField titleHighlight = new HighlightField("title");
        HighlightField summaryHighlight = new HighlightField("summary");
        query.setHighlightQuery(
                new HighlightQuery(new Highlight(List.of(titleHighlight, summaryHighlight)), Article.class));

        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);

        return searchHits.getSearchHits().stream()
                .map(hit -> {
                    Article article = hit.getContent();
                    // Replace content with highlights if available
                    if (hit.getHighlightFields().containsKey("title")) {
                        article.setTitle(String.join("...", hit.getHighlightFields().get("title")));
                    }
                    if (hit.getHighlightFields().containsKey("summary")) {
                        article.setSummary(String.join("...", hit.getHighlightFields().get("summary")));
                    }
                    return article;
                })
                .collect(Collectors.toList());
    }
}
