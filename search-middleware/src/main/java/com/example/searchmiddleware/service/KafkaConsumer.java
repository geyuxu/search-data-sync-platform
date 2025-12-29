package com.example.searchmiddleware.service;

import com.example.searchmiddleware.model.Article;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ArticleSyncService articleSyncService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "news-raw", groupId = "search-middleware-group")
    public void listen(String message) {
        try {
            log.info("Received message: {}", message);
            Article article = objectMapper.readValue(message, Article.class);

            // Normalize Date Format for Elasticsearch
            if (article.getPublishedDate() != null) {
                try {
                    ZonedDateTime zdt = ZonedDateTime.parse(article.getPublishedDate(),
                            DateTimeFormatter.RFC_1123_DATE_TIME);
                    article.setPublishedDate(zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                } catch (Exception e) {
                    log.warn("Failed to parse date '{}'. Using current time.", article.getPublishedDate());
                    article.setPublishedDate(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                }
            }

            articleSyncService.addToBuffer(article);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
