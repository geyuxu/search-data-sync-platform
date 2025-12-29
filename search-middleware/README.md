# Search Middleware

Spring Boot application to sync data from Kafka to MongoDB and Elasticsearch.

## Prerequisites
- Java 17+
- Maven

## Configuration
- **Kafka**: `localhost:9092`, Topic `news-raw`, Group `search-middleware-group`
- **MongoDB**: `localhost:27017`
- **Elasticsearch**: `localhost:9200`

## Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

## API
- **Search**: `GET /api/search?q=keyword`
