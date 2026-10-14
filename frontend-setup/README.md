# Tạo frontend cho Lab 1

Script tạo dự án Angular 20 bằng Angular CLI rồi chép code mẫu (màn hình Product) vào.
Làm theo cách này để luôn có cấu hình Angular chuẩn do CLI sinh ra.

```bash
# macOS / Linux / Git Bash (chạy ở thư mục gốc repo Lab 1)
bash frontend-setup/setup-frontend.sh

# Windows PowerShell
powershell -ExecutionPolicy Bypass -File frontend-setup\setup-frontend.ps1
```

Sau khi chạy:
- `frontend/src/app/product/` – danh sách và form sản phẩm (pattern mẫu)
- `frontend/src/app/core/api/product-api.service.ts` – service gọi API
- `frontend/proxy.conf.json` – `/api` chuyển tới `http://localhost:8080`

Kiểm tra: `cd frontend && npm test -- --watch=false` (cần Chrome cho Karma) và `npm start`.
Commit thư mục `frontend/` vào repo rồi có thể xóa thư mục `frontend-setup/`.
