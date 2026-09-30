#!/usr/bin/env bash
# Generate Gradle wrapper locally if you have Gradle installed.
set -euo pipefail
cd "$(dirname "$0")/.."
gradle wrapper --gradle-version 8.5
chmod +x gradlew
echo "gradlew ready"
