#!/usr/bin/env bash
# Tạo frontend Angular 20 cho Lab 1 và chép màn hình Product mẫu vào.
# Chạy từ bất kỳ đâu: bash frontend-setup/setup-frontend.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_DIR"

if [ -d frontend ]; then
  echo "Thư mục frontend/ đã tồn tại. Xóa hoặc đổi tên trước khi chạy lại." >&2
  exit 1
fi

# 1. Tạo dự án Angular (không hỏi tương tác)
npx -y @angular/cli@20 new frontend --routing --style=css --ssr=false --skip-git --defaults

# 2. Chép code mẫu (ghi đè app.routes.ts, app.config.ts, app.html, app.css)
cp -R "$SCRIPT_DIR/src/app/." frontend/src/app/
cp "$SCRIPT_DIR/proxy.conf.json" frontend/

# 3. npm start dùng proxy /api -> http://localhost:8080
cd frontend
npm pkg set scripts.start="ng serve --proxy-config proxy.conf.json"

echo "Xong. Chạy backend trước, sau đó: cd frontend && npm start  (http://localhost:4200)"
