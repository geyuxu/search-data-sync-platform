package com.example.searchmiddleware.service;

import com.example.searchmiddleware.model.Article;
import com.example.searchmiddleware.repository.es.ArticleEsRepository;
import com.example.searchmiddleware.repository.jpa.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleSyncService {

    private final ArticleRepository mysqlRepository;
    private final ArticleEsRepository esRepository;

    private final BlockingQueue<Article> buffer = new LinkedBlockingQueue<>();
    private static final int BATCH_SIZE = 5;

    public void addToBuffer(Article article) {
        buffer.offer(article);
        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    @Scheduled(fixedRate = 1000)
    public synchronized void flush() {
        while (!buffer.isEmpty()) {
            List<Article> articlesToSave = new ArrayList<>();
            buffer.drainTo(articlesToSave, BATCH_SIZE);

            if (articlesToSave.isEmpty()) {
                break;
            }

            try {
                log.info("Flushing {} articles...", articlesToSave.size());

                // 1. Save to MySQL (Source of Truth)
                // Note: Phase 2 - We introduced CDC.
                // Now this service ONLY writes to MySQL.
                // The CdcConsumer will handle the sync to ES asynchronously.
                List<Article> savedArticles = mysqlRepository.saveAll(articlesToSave);

                // esRepository.saveAll(savedArticles); <--- REMOVED DUAL WRITE

                log.info("Flushed {} articles to MySQL. CDC will sync to ES.", savedArticles.size());
            } catch (Exception e) {
                log.error("Error during flush", e);
                // TODO: Handle failure (e.g., retry or dead letter queue)
            }
        }
    }

    public synchronized String rebuildIndex() {
        log.info("Starting full index rebuild from MySQL...");
        long count = 0;
        try {
            esRepository.deleteAll(); // Dictionary clear (Danger!)

            int page = 0;
            int size = 100;
            org.springframework.data.domain.Page<Article> articlePage;

            do {
                articlePage = mysqlRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
                List<Article> content = articlePage.getContent();
                if (!content.isEmpty()) {
                    // Sanitize dates for ES
                    for (Article article : content) {
                        if (article.getPublishedDate() != null) {
                            try {
                                java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(article.getPublishedDate(),
                                        java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME);
                                article.setPublishedDate(
                                        zdt.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                            } catch (Exception e) {
                                // Already ISO or invalid, skip
                            }
                        }
                    }
                    esRepository.saveAll(content);
                    count += content.size();
                    log.info("Re-indexed batch {} ({} records)", page, content.size());
                }
                page++;
            } while (articlePage.hasNext());

            log.info("Rebuild completed. Total records: {}", count);
            return "Rebuild completed. Total records: " + count;
        } catch (Exception e) {
            log.error("Rebuild failed", e);
            throw new RuntimeException("Rebuild failed", e);
        }
    }
}
