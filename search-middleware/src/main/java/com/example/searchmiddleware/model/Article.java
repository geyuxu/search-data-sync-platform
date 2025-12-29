package com.example.searchmiddleware.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@org.springframework.data.mongodb.core.mapping.Document(collection = "articles")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "articles")
public class Article {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Keyword)
    private String link;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    @Field(type = FieldType.Date)
    @JsonProperty("published_date")
    private String publishedDate;

    @Field(type = FieldType.Keyword)
    private String author;
}
