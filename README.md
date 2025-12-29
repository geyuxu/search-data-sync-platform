# Search Data Sync Platform

A high-performance middleware system that synchronizes external RSS data into a searchable index (Elasticsearch) and persistent storage (MongoDB), featuring a modern Vue.js search frontend.

## Project Highlights

- **Decoupled Architecture**: Using Kafka to buffer writes, protecting the search engine from traffic spikes (solving the "OOM" and "Tombstone" issues typical in direct-write architectures).
- **Dual-Write Consistency**: Implemented "Source of Truth" (MongoDB) + "Search View" (ES) pattern to ensure data integrity and re-indexability.
- **High Performance**: Custom batch processing logic in the middleware layer to optimize I/O throughput.

## Architecture

```mermaid
graph LR
    A[Crawler (Python)] -->|JSON| B(Kafka Topic: news-raw)
    
    subgraph "Middleware (Java Spring Boot)"
    C[Kafka Consumer] -->|Batch Fetch| B
    D[Processing Logic] -->|Clean & Format| C
    end
    
    D -->|Upsert| E[(MongoDB)]
    D -->|Index| F[(Elasticsearch)]
    
    G[Frontend (Vue.js)] -->|Search API| H[REST Controller]
    H -->|Query| F
```

## System Requirements

- **Java 17+**
- **Python 3.8+**
- **Docker & Docker Compose**

## Quick Start Guide

### 1. Start Infrastructure (Docker)
Spin up Kafka, Zookeeper, Elasticsearch, Kibana, and MongoDB.

```bash
docker-compose up -d
```
> **Note**: Kafka is exposed on port **9094** to avoid conflicts.

### 2. Run Data Crawler
The Python crawler fetches RSS feeds (Hacker News) and pushes them to Kafka.

```bash
# Install dependencies
pip3 install -r requirements.txt

# Run crawler
python3 rss_crawler.py
```

### 3. Run Search Middleware
The Java backend processes the stream and exposes the Search API.

```bash
cd search-middleware
./mvnw spring-boot:run
```
> The API will be available at `http://localhost:8080/api`.

### 4. Run Search Frontend
A minimalist Vue.js interface to search the indexed data.

For strict CORS security, it's best to run this via a local server:

```bash
cd search-frontend
python3 -m http.server 3000
```
Then open **[http://localhost:3000](http://localhost:3000)** in your browser.

## API Reference

### Get Recent Articles
`GET /api/recent`
Returns the 10 most recently indexed articles.

### Search Articles
`GET /api/search?q={keyword}`
Returns articles matching the keyword with highlighting.

| Parameter | Description |
|-----------|-------------|
| `q`       | Search query (e.g., "java") |

## Troubleshooting

- **CORS Errors**: Ensure you have restarted the backend after any configuration changes. Running the frontend via `python3 -m http.server` resolves most file-system CORS issues.
- **No Data**: Check `rss_crawler.py` output. If it successfully sent messages, check the middleware logs for "Received message".
- **Kafka Connection**: Ensure `docker-compose` is running and port `9094` is accessible.
