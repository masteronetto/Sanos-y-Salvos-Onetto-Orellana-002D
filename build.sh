#!/bin/bash
set -e
cd "$(dirname "$0")"
echo "Limpiando cache de Gradle..."
rm -rf .gradle build
echo "Ejecutando build..."
./gradlew clean build -x test
echo "Build completado exitosamente"
