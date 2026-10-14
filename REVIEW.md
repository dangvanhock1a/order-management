# Hướng dẫn review

## Thế nào là "Important" (nghiêm trọng)
Chỉ dành cho lỗi làm sai hành vi, lộ dữ liệu hoặc chặn rollback: logic sai, truy vấn không lọc theo
quyền người dùng, dữ liệu cá nhân trong log/response, migration không tương thích ngược.
Style, đặt tên, gợi ý refactor tối đa là "Nit".

## Giới hạn Nit
Tối đa 5 Nit mỗi lần review. Nhiều hơn thì ghi "thêm N mục tương tự" trong phần tóm tắt.

## Không báo
- Những gì CI đã kiểm tra: lint, format, lỗi kiểu.
- File sinh tự động trong target/, dist/, và file *.lock.

## Luôn kiểm tra
- API mới có test tích hợp.
- Mỗi BR-xx được nhắc trong mô tả PR có test tương ứng.
- Log không chứa email, số điện thoại, request body.
