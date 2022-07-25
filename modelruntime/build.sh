#! /bin/bash

echo "Building..."

./gradlew clean assemble

echo "Building docker image..."
docker build --tag=modelruntime:latest .
