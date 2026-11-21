#!/usr/bin/env bash
input=$(cat)
[[ $(echo "$input" | jq -r '.stop_hook_active') == "true" ]] && exit 0
cd "$CLAUDE_PROJECT_DIR" || exit 0
out=$(./gradlew -q detekt 2>&1) && exit 0
echo "$out" | tail -40 >&2
exit 2
