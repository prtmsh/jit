#!/bin/bash

# Build project
mvn clean package

# Create install directories
sudo mkdir -p /usr/local/lib/jit
sudo mkdir -p /usr/local/bin

# Install JAR
sudo cp target/jit-1.0.0-jar-with-dependencies.jar /usr/local/lib/jit/jit.jar

# Install launcher
sudo cp bin/jit /usr/local/bin/
sudo chmod +x /usr/local/bin/jit

echo "Jit installed successfully!"
echo "Run 'jit init' to initialize a new repository"