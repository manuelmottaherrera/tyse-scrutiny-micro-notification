#!/bin/bash

################################################################################
# CI Local - Micro Notification (Mínimo)
#
# Este script ejecuta los tests del microservicio de notificaciones.
# Es un script mínimo porque el proyecto no tiene:
#   - Base de datos (PostgreSQL)
#   - Frontend
#   - Docker compose files
#
# Solo ejecuta:
#   - Unit tests (Maven Surefire)
#   - Integration tests (Maven Failsafe)
#
# Uso:
#   ./scripts/ci-local.sh
################################################################################

set -e  # Exit on error
set -o pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Track timing
SCRIPT_START=$(date +%s)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Micro Notification - CI Pipeline (Mínimo)${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

################################################################################
# Pre-flight: Clean Environment
################################################################################

echo -e "${YELLOW}[Pre-flight] Cleaning CI environment...${NC}"
CLEAN_START=$(date +%s)

# 1. Limpiar directorio target/ (artefactos de Maven)
if [ -d "target/" ]; then
    echo "  → Removing target/ directory..."
    rm -rf target/
fi

# 2. Limpiar archivos temporales de build
echo "  → Removing temporary build files..."
rm -rf build/ 2>/dev/null || true

# 3. Matar procesos Java remanentes (del puerto 8085 - notification)
JAVA_PID=$(lsof -ti:8085 2>/dev/null || true)
if [ -n "$JAVA_PID" ]; then
    echo "  → Killing Java process on port 8085 (PID: $JAVA_PID)..."
    kill -9 $JAVA_PID 2>/dev/null || true
    sleep 2
fi

CLEAN_END=$(date +%s)
CLEAN_TIME=$((CLEAN_END - CLEAN_START))

echo -e "${GREEN}✓ Environment cleaned${NC} (${CLEAN_TIME}s)"
echo ""

################################################################################
# Job 1: Backend Tests
################################################################################

echo -e "${YELLOW}[Job 1/1] Starting Backend Tests...${NC}"
BACKEND_START=$(date +%s)

echo "  → Running Maven verify..."
echo "     - Unit tests (Surefire)"
echo "     - Integration tests (Failsafe)"
echo ""

./mvnw clean verify \
  -Dlogging.level.ROOT=ERROR \
  -Dlogging.level.tech.jhipster=ERROR \
  -Dlogging.level.com.tyse.scrutiny=ERROR

BACKEND_END=$(date +%s)
BACKEND_TIME=$((BACKEND_END - BACKEND_START))

echo ""
echo -e "${GREEN}✓ Backend Tests passed${NC} (${BACKEND_TIME}s)"
echo ""

# Check if test results exist
if [ -d "target/surefire-reports/" ]; then
    UNIT_TESTS=$(find target/surefire-reports/ -name "TEST-*.xml" 2>/dev/null | wc -l)
    echo "  → Unit test results: target/surefire-reports/ (${UNIT_TESTS} test suites)"
fi

if [ -d "target/failsafe-reports/" ]; then
    IT_TESTS=$(find target/failsafe-reports/ -name "TEST-*.xml" 2>/dev/null | wc -l)
    if [ "$IT_TESTS" -gt 0 ]; then
        echo "  → Integration test results: target/failsafe-reports/ (${IT_TESTS} test suites)"
    fi
fi

echo ""

################################################################################
# Summary
################################################################################

SCRIPT_END=$(date +%s)
TOTAL_TIME=$((SCRIPT_END - SCRIPT_START))

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ CI Pipeline Passed${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Timing Summary:"
echo "  Environment Cleanup:     ${CLEAN_TIME}s"
echo "  Backend Tests:           ${BACKEND_TIME}s"
echo "  ───────────────────────────────────"
echo "  Total:                   ${TOTAL_TIME}s"
echo ""
echo "All checks passed! ✓"
echo "The code is ready to be pushed to GitHub."
echo ""

exit 0
