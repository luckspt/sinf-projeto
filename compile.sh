#!/bin/bash
# Create targe/classes directory if it does not exist
mkdir -p target/classes

# Compile the source code
javac -cp src/main/java -d target/classes \
src/main/java/pt/fcul/sinf/si003/client/myCloud.java \
src/main/java/pt/fcul/sinf/si003/server/myCloudServer.java