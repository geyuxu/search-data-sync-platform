package com.geyuxu.searchmiddleware.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "articles")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "articles")
public class Article {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @jakarta.persistence.Column(length = 50)
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String title;

    @Field(type = FieldType.Keyword)
    @jakarta.persistence.Column(length = 500)
    private String link;

    @Field(type = FieldType.Text, analyzer = "standard")
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String summary;

    @Field(type = FieldType.Date)
    @JsonProperty("published_date")
    @jakarta.persistence.Column(name = "published_date")
    private String publishedDate;

    @Field(type = FieldType.Keyword)
    private String author;
}
