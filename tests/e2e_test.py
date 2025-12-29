import time
import uuid
import json
import pytest
import requests
from kafka import KafkaProducer
from elasticsearch import Elasticsearch
import mysql.connector

# Configuration
KAFKA_BOOTSTRAP_SERVERS = 'localhost:9094'
KAFKA_TOPIC = 'news-raw'
ES_HOST = 'http://localhost:9200'
MYSQL_CONFIG = {
    'user': 'user',
    'password': 'password',
    'host': 'localhost',
    'database': 'search_db',
    'port': 3306
}
SEARCH_API_URL = 'http://localhost:8080/api/search'

@pytest.fixture(scope="module")
def kafka_producer():
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )
    yield producer
    producer.close()

@pytest.fixture(scope="module")
def es_client():
    es = Elasticsearch([ES_HOST])
    yield es
    # Cleanup if needed

@pytest.fixture(scope="module")
def mysql_conn():
    conn = mysql.connector.connect(**MYSQL_CONFIG)
    yield conn
    conn.close()

def test_end_to_end_flow(kafka_producer, es_client, mysql_conn):
    # 1. Produce a unique article
    unique_id = str(uuid.uuid4())
    test_title = f"E2E Test Article {unique_id}"
    test_link = f"http://test.com/{unique_id}"
    
    article = {
        'title': test_title,
        'link': test_link,
        'summary': f"Summary for {unique_id}",
        'published_date': time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        'author': 'e2e-tester'
    }
    
    print(f"\n[Step 1] Sending article to Kafka: {test_title}")
    kafka_producer.send(KAFKA_TOPIC, article)
    kafka_producer.flush()
    
    # 2. Wait for processing
    print("[Step 2] Waiting for Middleware processing (5s)...")
    time.sleep(5)
    
    # 3. Verify MySQL
    print("[Step 3] Verifying MySQL...")
    cursor = mysql_conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM articles WHERE link = %s", (test_link,))
    result = cursor.fetchone()
    assert result is not None, "Article not found in MySQL"
    assert result['title'] == test_title, "Title mismatch in MySQL"
    print(" -> Found in MySQL")
    
    # 4. Verify Elasticsearch
    print("[Step 4] Verifying Elasticsearch...")
    # Refresh index ensuring data is searchable
    es_client.indices.refresh(index="_all")
    
    # Simple search or get by id (if we knew the ID, but we only know the link)
    # Using search query
    es_response = es_client.search(index="articles", body={
        "query": {
            "match_phrase": {
                "link": test_link
            }
        }
    })
    
    hits = es_response['hits']['hits']
    assert len(hits) > 0, "Article not found in Elasticsearch"
    assert hits[0]['_source']['title'] == test_title, "Title mismatch in Elasticsearch"
    print(" -> Found in Elasticsearch")
    
    # 5. Verify Search API
    print("[Step 5] Verifying Search API...")
    # Search for "E2E" to ensure we match the tokenized title
    response = requests.get(SEARCH_API_URL, params={'q': "E2E"})
    assert response.status_code == 200, f"Search API failed with {response.status_code}"
    data = response.json()
    
    found = False
    for item in data:
        if item['link'] == test_link:
            found = True
            break
    
    assert found, "Article not found via Search API"
    print(" -> Found via Search API")

if __name__ == "__main__":
    pytest.main([__file__])
