# Lab 1 – Quản lý đơn hàng

Repo dùng cho các bài Lab 1.1 → 1.6 trong Bộ 2 (Phần B).

## Chạy lần đầu (Windows PowerShell)
Cần PostgreSQL 16+ cài trên máy, cổng 5432, `psql` có trong PATH (Bộ 2, mục A2).
```powershell
psql -U postgres -v ON_ERROR_STOP=1 -f db/create-db.sql   # tạo database orderdb + user orderapp (nhập mật khẩu postgres)
cd backend
.\mvnw.cmd verify                       # build + unit test
.\mvnw.cmd spring-boot:run              # Flyway tạo bảng; API tại http://localhost:8080/api/products
cd ..\frontend; npm ci; npm start       # giao diện tại http://localhost:4200
```

## Bắt đầu với Claude Code
```bash
claude --permission-mode plan
```
Gõ `/context` để kiểm tra CLAUDE.md đã được nạp. Skill có sẵn: `/chuan-hoa-spec`. Subagent có sẵn: `code-reviewer`.

## Thư mục
- `input/` – requirement thô của khách (đầu vào Lab 1.1)
- `templates/` – template spec, Q&A, basic design, detail design
- `docs/` – nơi lưu spec, design, test case, WBS do các bài lab tạo ra
- `customer-templates/khach-a/` – template Excel của khách (Lab 1.3)
- `backend/`, `frontend/` – mã nguồn; chức năng Product là pattern mẫu
