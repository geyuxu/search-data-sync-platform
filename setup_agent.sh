#!/bin/bash
# Download SkyWalking Java Agent 9.0.0
echo "Downloading SkyWalking Java Agent..."
curl -L -O https://archive.apache.org/dist/skywalking/java-agent/9.0.0/apache-skywalking-java-agent-9.0.0.tgz

# Extract
echo "Extracting..."
tar -zxvf apache-skywalking-java-agent-9.0.0.tgz

# Clean up
rm apache-skywalking-java-agent-9.0.0.tgz

echo "SkyWalking Agent is ready in 'skywalking-agent' directory."
echo "You can now run the app with tracing enabled."
