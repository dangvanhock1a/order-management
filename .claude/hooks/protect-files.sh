#!/bin/bash
# Chặn Claude sửa file nhạy cảm. Exit code 2 = chặn và báo lý do cho Claude.
# Cần cài jq. Trên macOS/Linux chạy: chmod +x .claude/hooks/protect-files.sh
INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
FILE_PATH="${FILE_PATH//\\//}"
PROTECTED_PATTERNS=(".env" "application-prod.yml" ".git/")

for pattern in "${PROTECTED_PATTERNS[@]}"; do
  if [[ "$FILE_PATH" == *"$pattern"* ]]; then
    echo "Blocked: $FILE_PATH khớp mẫu được bảo vệ '$pattern'" >&2
    exit 2
  fi
done
exit 0
