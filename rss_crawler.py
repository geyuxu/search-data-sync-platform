import time
import json
import feedparser
from kafka import KafkaProducer

import os

# Configuration
RSS_URL = "https://news.ycombinator.com/rss"  # Hacker News RSS
KAFKA_BOOTSTRAP_SERVERS = os.environ.get('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9094').split(',')
KAFKA_TOPIC = 'news-raw'
POLL_INTERVAL_SECONDS = 300  # 5 minutes

def get_kafka_producer():
    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
        return producer
    except Exception as e:
        print(f"Error connecting to Kafka: {e}")
        return None

def fetch_rss_feed(url):
    try:
        feed = feedparser.parse(url)
        return feed.entries
    except Exception as e:
        print(f"Error fetching RSS feed: {e}")
        return []

def main():
    print("Starting RSS Crawler...")
    producer = get_kafka_producer()
    if not producer:
        print("Failed to initialize Kafka producer. Exiting.")
        return

    visited_links = set()

    while True:
        print(f"Fetching RSS feed from {RSS_URL}...")
        entries = fetch_rss_feed(RSS_URL)
        
        new_entries_count = 0
        for entry in entries:
            link = entry.get('link')
            if link and link not in visited_links:
                visited_links.add(link)
                
                news_item = {
                    'title': entry.get('title'),
                    'link': link,
                    'summary': entry.get('summary', ''),
                    'published_date': entry.get('published', ''),
                    'author': entry.get('author', 'unknown') # Author might not be present in all feeds
                }
                
                try:
                    producer.send(KAFKA_TOPIC, news_item)
                    new_entries_count += 1
                except Exception as e:
                    print(f"Error sending to Kafka: {e}")

        if new_entries_count > 0:
            print(f"Sent {new_entries_count} new articles to Kafka.")
            producer.flush()
        else:
            print("No new articles found.")

        print(f"Sleeping for {POLL_INTERVAL_SECONDS} seconds...")
        time.sleep(POLL_INTERVAL_SECONDS)

if __name__ == "__main__":
    main()
