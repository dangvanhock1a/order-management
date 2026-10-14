# Order Management (Lab 1)

## Tổng quan
- Hệ thống quản lý đơn hàng: khách hàng, sản phẩm, đơn hàng, tồn kho.
- backend/: Java 21, Spring Boot 3, Spring Data JPA, Flyway, PostgreSQL.
- frontend/: Angular. Gọi REST API của backend qua /api (proxy khi chạy npm start).
- Chức năng Product đã có sẵn, là PATTERN MẪU cho mọi chức năng mới.

## Lệnh thường dùng
- DB local: PostgreSQL cài trên máy, cổng 5432, database orderdb, user orderapp (tạo bằng db/create-db.sql)
- Backend build + test toàn bộ: cd backend && ./mvnw verify
- Backend chạy 1 test: ./mvnw test -Dtest=ProductServiceTest
- Backend chạy app: ./mvnw spring-boot:run
- Frontend: cd frontend && npm ci && npm test -- --watch=false
- Frontend chạy: npm start   (http://localhost:4200)

## Quy ước code
- Package theo chức năng: com.example.order.<feature>.{controller,service,repository,domain,dto}
- Controller không chứa business logic. Validate input bằng Bean Validation trên DTO.
- Không trả Entity ra API, dùng DTO (record). Danh sách trả về PageResponse.
- Lỗi nghiệp vụ: ném BusinessException(code, message) → HTTP 409. Không tìm thấy: NotFoundException → 404.
- Thay đổi DB chỉ qua Flyway: V<số>__<mo_ta>.sql. Không sửa migration đã merge.
- Business rule đánh số BR-xx theo spec. Tên test: should_<kết quả>_when_<điều kiện>_BRxx.
- Frontend: standalone component, Reactive Forms, gọi API qua service trong src/app/core/api.

## Quy trình
- Thay đổi từ 3 file trở lên: lập kế hoạch ở plan mode, chờ duyệt.
- Trước khi báo xong: chạy ./mvnw verify, báo kèm output.
- Không tự giả định nghiệp vụ. Chỗ chưa rõ ghi vào docs/spec/qa.md.

## Tài liệu
- Requirement thô: input/ · Spec: docs/spec/ · Design: docs/design/ · Test case: docs/test/
- Template: templates/

## Không được
- Không sửa file migration đã có trên nhánh main.
- Không commit .env, application-local.yml.
