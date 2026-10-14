\encoding UTF8
-- Tạo database và user cho Lab 1 (Quản lý đơn hàng) trên PostgreSQL cài trên máy (cổng 5432).
-- Chạy bằng user quản trị postgres. PowerShell, tại thư mục gốc repo Lab 1:
--   psql -U postgres -v ON_ERROR_STOP=1 -f db/create-db.sql
-- Mật khẩu dưới đây chỉ dùng cho máy lab. Bảng do Flyway tạo khi backend khởi động.

CREATE ROLE orderapp LOGIN PASSWORD 'orderapp';
CREATE DATABASE orderdb OWNER orderapp TEMPLATE template0 ENCODING 'UTF8';

-- Kiểm tra (mật khẩu: orderapp):
--   psql -U orderapp -d orderdb -c "select current_user, version();"
-- Làm lại từ đầu:
--   psql -U postgres -c "DROP DATABASE IF EXISTS orderdb;" -c "DROP ROLE IF EXISTS orderapp;"
