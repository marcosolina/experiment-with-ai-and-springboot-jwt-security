#!/usr/bin/env bash

# Generate a JWT token signed with the shared secret key (HS256)
# Usage: ./generate-token.sh [--username <name>] [--roles <role1,role2>] [--expiry <hours>]
#
# Examples:
#   ./generate-token.sh
#   ./generate-token.sh --username myuser --roles USER,ADMIN --expiry 2

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if ! command -v node &> /dev/null; then
    echo "Error: Node.js is required but not installed."
    exit 1
fi

# Install dependencies if needed
if [ ! -d "$SCRIPT_DIR/node_modules" ]; then
    echo "Installing dependencies..."
    cd "$SCRIPT_DIR" && npm install
fi

node "$SCRIPT_DIR/generate-token.js" "$@"
