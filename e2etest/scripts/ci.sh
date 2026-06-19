#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "=== Installing dependencies ==="
npm ci

echo "=== Installing Playwright browsers ==="
npx playwright install --with-deps chromium

echo "=== Running E2E tests ==="
npx playwright test
