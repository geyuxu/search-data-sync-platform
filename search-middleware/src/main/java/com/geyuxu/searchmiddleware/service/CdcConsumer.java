package com.geyuxu.searchmiddleware.service;

import com.geyuxu.searchmiddleware.model.Article;
import com.geyuxu.searchmiddleware.repository.es.ArticleEsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CdcConsumer {

    private final ArticleEsRepository esRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "dbserver1.search_db.articles", groupId = "search-cdc-group")
    public void handleCdcEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode payload = root;
            if (root.has("payload")) {
                payload = root.path("payload");
            }
            JsonNode after = payload.path("after");
            String op = payload.path("op").asText();

            // 'd' = delete, 'c' = create, 'u' = update, 'r' = read (snapshot)
            if ("d".equals(op)) {
                JsonNode before = payload.path("before");
                String id = before.path("id").asText();
                esRepository.deleteById(id);
                log.info("CDC: Deleted article {}", id);
                return;
            }

            if (!after.isMissingNode()) {
                // Convert JSON (MySQL column names) to Article object
                // Note: @JsonProperty("published_date") on Article.java handles the snake_case
                // mapping
                Article article = objectMapper.convertValue(after, Article.class);

                esRepository.save(article);
                log.info("CDC: Synced article {}", article.getId());
            }

        } catch (Exception e) {
            log.error("Failed to process CDC event: {}", message, e);
        }
    }
}
