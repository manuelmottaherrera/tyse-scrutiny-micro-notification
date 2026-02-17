#!/bin/bash

################################################################################
# Push Script - CI + Git Push (Micro Notification)
#
# Este script ejecuta el CI del microservicio de notificaciones y hace push
# solo si todos los tests pasan.
#
# Es un script mínimo porque el proyecto no tiene:
#   - Base de datos
#   - Frontend
#   - Docker
#
# Uso:
#   ./scripts/push.sh                       # Push a origin/current-branch
#   ./scripts/push.sh origin develop        # Push a origin/develop
#   ./scripts/push.sh --skip-ci             # Push sin ejecutar CI
#
# Opciones:
#   --skip-ci            Saltar CI y hacer push directamente
################################################################################

set -e  # Exit on error
set -o pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Log file configuration
LOG_DIR="logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="${LOG_DIR}/push_${TIMESTAMP}.log"

# Create logs directory if it doesn't exist
mkdir -p "$LOG_DIR"

# Function to log both to console and file (stripping color codes from file)
log() {
    echo -e "$@" | tee -a >(sed 's/\x1b\[[0-9;]*m//g' >> "$LOG_FILE")
}

# Parse arguments
SKIP_CI=false
REMOTE="origin"
BRANCH=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-ci)
            SKIP_CI=true
            shift
            ;;
        *)
            if [ -z "$REMOTE" ] || [ "$REMOTE" = "origin" ]; then
                REMOTE="$1"
            elif [ -z "$BRANCH" ]; then
                BRANCH="$1"
            fi
            shift
            ;;
    esac
done

# Get current branch if not specified
if [ -z "$BRANCH" ]; then
    BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
    if [ -z "$BRANCH" ]; then
        log "${RED}❌ Error: No se pudo detectar la rama actual${NC}"
        exit 1
    fi
fi

log ""
log "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
log "${BLUE}║    Push Script - Micro Notification CI + Push      ║${NC}"
log "${BLUE}╚════════════════════════════════════════════════════╝${NC}"
log ""
log "${YELLOW}Remote:${NC} $REMOTE"
log "${YELLOW}Branch:${NC} $BRANCH"
log "${YELLOW}Log file:${NC} $LOG_FILE"
log ""

################################################################################
# Step 1: Ejecutar CI (si no se skip)
################################################################################

if [ "$SKIP_CI" = true ]; then
    log "${YELLOW}⚠️  Skipping CI validation (--skip-ci flag)${NC}"
    log ""
else
    log "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log "${YELLOW}Step 1: Running CI Pipeline${NC}"
    log "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log ""
    log "${YELLOW}⏱️  This may take 1-2 minutes...${NC}"
    log ""

    # Run CI script and capture output to log
    if ./scripts/ci-local.sh 2>&1 | tee -a >(sed 's/\x1b\[[0-9;]*m//g' >> "$LOG_FILE"); then
        log ""
        log "${GREEN}✅ CI Pipeline passed successfully!${NC}"
        log ""
    else
        log ""
        log "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        log "${RED}❌ CI Pipeline Failed!${NC}"
        log "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        log ""
        log "${YELLOW}Your code did not pass the CI checks.${NC}"
        log "${YELLOW}Please fix the errors before pushing.${NC}"
        log ""
        log "Options:"
        log "  1. Fix the failing tests"
        log "  2. Run ${BLUE}./scripts/ci-local.sh${NC} to debug"
        log "  3. Run ${BLUE}./scripts/push.sh --skip-ci${NC} to push without CI (not recommended)"
        log ""
        log "${YELLOW}Full log available at:${NC} $LOG_FILE"
        log ""
        exit 1
    fi
fi

################################################################################
# Step 2: Git Push
################################################################################

log "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log "${YELLOW}Step 2: Pushing to ${REMOTE}/${BRANCH}${NC}"
log "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log ""

# Execute git push and capture output
if git push "$REMOTE" "$BRANCH" 2>&1 | tee -a >(sed 's/\x1b\[[0-9;]*m//g' >> "$LOG_FILE"); then
    log ""
    log "${GREEN}╔════════════════════════════════════════════════════╗${NC}"
    log "${GREEN}║            ✅ Push Successful!                     ║${NC}"
    log "${GREEN}╚════════════════════════════════════════════════════╝${NC}"
    log ""
    log "${GREEN}Successfully pushed to ${REMOTE}/${BRANCH}${NC}"
    log "${YELLOW}Full log saved to:${NC} $LOG_FILE"
    log ""
    exit 0
else
    log ""
    log "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log "${RED}❌ Git Push Failed!${NC}"
    log "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log ""
    log "${YELLOW}Git push encountered an error.${NC}"
    log "${YELLOW}Check the error message above for details.${NC}"
    log "${YELLOW}Full log saved to:${NC} $LOG_FILE"
    log ""
    exit 1
fi
