#!/usr/bin/env bash
f=$(jq -r '.tool_input.file_path // empty')
[[ "$f" == *.kt || "$f" == *.kts ]] || exit 0
cd "$CLAUDE_PROJECT_DIR" || exit 0
./gradlew -q ktfmtFormat >/dev/null 2>&1 || true
exit 0
