# Detail Design – API-DH-04 Tạo đơn hàng
Phiên bản: 0.3 · Ngày: 2026-10-15 · Người viết: Dang Van Hoc

Lịch sử thay đổi:
- 0.1: bản đầu, có bảng quyết định riêng D-01 → D-11 chờ TL.
- 0.2: viết lại theo basic design v0.5 (TD đã được TL chọn) và spec đơn hàng v0.12.
  - Bỏ bảng D-xx, nguồn kỹ thuật nay là TD.
  - Lý do sửa giá tối đa 200 ký tự (Q-75); nhật ký chỉ ghi cho đơn (Q-74); mã lỗi 401/403 theo TD-15.
  - Thêm mã TC, giá trị biên và bảng truy vết.
  - Bỏ log ứng dụng mức INFO vì không có nguồn.
- 0.3: theo basic design v0.6 (TL chọn TD-01 A, TD-02 A, TD-05 A, TD-12 B, TD-13 A).
  - Thêm bước 1 kiểm tra CSRF, mã 403 `CSRF_TOKEN_INVALID`, header `X-XSRF-TOKEN`, TC-38. Các bước sau dịch thêm 1.
  - Thêm cột `version` (= 0 khi tạo) và trường `version` trong response.

Nguồn và ký hiệu:

| Ký hiệu | Nguồn |
|---------|-------|
| DH UC-xx / BR-xx / SCR-xx | `docs/spec/don-hang.md` v0.12 |
| BD, TD-xx | `docs/design/basic-design.md` v0.6 (TD-xx: mục 10) |
| QA: Q-xx | Câu trả lời đã đóng trong `docs/spec/qa.md` |
| TC-xx | Tình huống test ở mục 11 |

## 0. Quyết định kỹ thuật áp dụng
TL đã chọn, ghi tại BD mục 10.

| TD | Phương án đã chọn | Dùng ở mục |
|----|-------------------|------------|
| TD-01 | A. Spring Security session + cookie HttpOnly, có CSRF token | 2, 4, 5 (bước 1 → 3) |
| TD-02 | A. API tính thử ở BE; `PricingCalculator` dùng chung với API-DH-12 | 5 (bước 15) |
| TD-03 | A. Bảng `order_code_counter` + upsert `RETURNING` | 5 (bước 18), 6, 7 |
| TD-04 | B. `SELECT … FOR UPDATE` sản phẩm theo `id` tăng dần; chờ khóa tối đa 5 giây, quá thì 409 `CONCURRENT_UPDATE` | 4, 5 (bước 9, 17), 6 |
| TD-05 | A. Optimistic lock bằng `sales_order.version`; đơn mới có `version` = 0 | 3, 5 (bước 19), 7 |
| TD-06 | A. `AuditService` gọi tường minh trong cùng transaction | 8 |
| TD-07 | A. `before_data`, `after_data` kiểu `JSONB` | 7, 8 |
| TD-08 | A. Tính VIP bằng `SUM` khi cần, so với ngưỡng hiện tại | 5 (bước 14), 7 |
| TD-09 | A. Tham số trong `application.yml` + `@ConfigurationProperties` | 5 (bước 14 → 16), 9 |
| TD-10 | A. `NUMERIC(15,2)` + `BigDecimal`, CHECK số nguyên | 2, 5, 7 |
| TD-11 | A. `TIMESTAMPTZ` + `Clock` theo `Asia/Ho_Chi_Minh` | 3, 5 (bước 18), 7 |
| TD-14 | A. Header `Idempotency-Key` + cột UNIQUE + advisory lock; gửi lại thì trả 200 với đơn cũ | 2, 3, 4, 5 (bước 4, 7), 6, 7 |
| TD-15 | B. Mã riêng theo nguyên nhân cho 401/403 | 4, 5 (bước 1, 2, 3, 11) |
| TD-16 | A. Trùng `productId` trong request thì trả 400 | 2, 5 (bước 6) |
| TD-17 | A. Test tích hợp trên PostgreSQL local, database riêng cho test | 11 |

API này không phụ thuộc TD-12 (chuyển trạng thái) và TD-13 (xuất Excel).

## 1. Endpoint
- Method + Path: `POST /api/orders` (BD API-DH-04).
- Quyền:
  - Đã đăng nhập, tài khoản đang hoạt động, không ở trạng thái phải đổi mật khẩu (BD API-AUTH-01; QA: Q-60, Q-61, Q-64).
  - Vai trò Nhân viên hoặc Quản lý (DH BR-16; BD mục 7).
  - Đơn giá khác giá niêm yết: chỉ Quản lý (DH BR-03).
- Use case / BR: DH UC-01; BR-01 → BR-08, BR-12, BR-16, BR-19, BR-21, BR-25. BR-17 (định dạng hiển thị tiền) do FE xử lý.

## 2. Request

### Header

| Trường | Kiểu | Bắt buộc | Ràng buộc | Nguồn |
|--------|------|----------|-----------|-------|
| `X-XSRF-TOKEN` | String | Có | Bằng giá trị cookie `XSRF-TOKEN` do server cấp. Angular `HttpClient` tự gửi | TD-01; BD mục 5 (cấu hình Spring Security) |
| `Idempotency-Key` | String (UUID) | Có | Đúng định dạng UUID: 36 ký tự `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`, `x` là chữ số hex. FE sinh mới mỗi lần mở SCR-DH-01 để tạo đơn và gửi lại đúng key khi thử lại | TD-14 |

Trình duyệt tự gửi kèm cookie phiên (HttpOnly) và cookie `XSRF-TOKEN` (TD-01).

### Body (`CreateOrderRequest`, record)

| Trường | Kiểu | Bắt buộc | Ràng buộc | Nguồn |
|--------|------|----------|-----------|-------|
| `customerId` | Số nguyên (Long) | Có | Không null. Không tồn tại thì 404 ở bước 8 | DH BR-01; UC-01 E1, E4 |
| `lines` | Mảng `OrderLineRequest` | Có | Ít nhất 1 phần tử. Không có giới hạn số phần tử tối đa (spec không quy định). Không có 2 phần tử cùng `productId` | DH BR-01, BR-02; TD-16 |
| `lines[].productId` | Số nguyên (Long) | Có | Không null. Không tồn tại thì 404 ở bước 9 | DH BR-01; UC-01 E4 |
| `lines[].quantity` | Số nguyên (Integer) | Có | Min 1; max 2.147.483.647 (giới hạn kiểu Integer). Không nhận số có phần thập phân (ví dụ `1.5`) hay chuỗi. Không có giới hạn trên nghiệp vụ; tồn kho chặn ở bước 13 | DH BR-02, BR-12; UC-01 E2; BD mục 5 (`accept-float-as-int: false`) |
| `lines[].unitPrice` | Số (BigDecimal) | Không | Nếu có: min 0; không có phần thập phân; tối đa 13 chữ số phần nguyên (cột `NUMERIC(15,2)`). Không gửi thì đơn giá = giá niêm yết tại lúc lưu. Khác giá niêm yết: chỉ Quản lý (bước 11) | DH BR-03, BR-25; UC-01 E6; BD API-DH-04, mục 6; TD-10 |
| `lines[].priceChangeReason` | Chuỗi | Khi `unitPrice` khác giá niêm yết (kiểm tra ở bước 12) | Tối đa 200 ký tự (kiểm tra ở bước 6, kể cả khi không bắt buộc). Khi bắt buộc: không rỗng, không chỉ gồm khoảng trắng. Dòng không đổi giá thì không lưu lý do (null) | DH BR-03, SCR-01, mục 7; QA: Q-75 |

Ví dụ:

```json
POST /api/orders
X-XSRF-TOKEN: 6b1f0c2e-…
Idempotency-Key: 3f1c9a52-7d0e-4c55-9a43-2b8f1e6d7a10

{
  "customerId": 12,
  "lines": [
    { "productId": 3, "quantity": 2 },
    { "productId": 4, "quantity": 1, "unitPrice": 15000000, "priceChangeReason": "Giá khuyến mãi khai trương" }
  ]
}
```

## 3. Response

| HTTP | Khi nào | Nguồn |
|------|---------|-------|
| 201 Created | Tạo đơn mới | Pattern `ProductController.create` |
| 200 OK | `Idempotency-Key` đã gắn với một đơn: trả lại đơn đó, không tạo đơn mới | TD-14 |

Body (`OrderResponse`, record). Số tiền trả dạng số nguyên, không có phần `.00`:

| Trường | Kiểu | Mô tả | Nguồn |
|--------|------|-------|-------|
| `id` | Long | Id đơn, để FE mở SCR-DH-03 | BD mục 4.1 (Lưu thành công → SCR-DH-03) |
| `code` | String | `PBH-YYYYMMDD-NNN`, từ đơn thứ 1000 có 4 chữ số | DH BR-21 |
| `status` | String | `NEW` hoặc `PENDING_APPROVAL` | DH BR-08; BD mục 6 |
| `version` | Long | Phiên bản đơn, bằng 0 khi mới tạo. FE gửi kèm khi sửa (API-DH-05) hoặc chuyển trạng thái (API-DH-06) | TD-05; BD API-DH-03, mục 6 |
| `customer` | Object | `id`, `name`, `phone` | DH SCR-01 (Khách hàng), mục 7 |
| `vip` | Boolean | Trạng thái VIP xét lúc lưu | DH SCR-01 (Cờ VIP), BR-25; BD mục 6 (`sales_order.vip`) |
| `subtotal` | Số nguyên | Tạm tính | DH BR-04, mục 7 |
| `discountRate` | Số thập phân | Tỷ lệ giảm đã áp dụng, ví dụ `0.1`; khách không VIP là `0` | DH BR-05, mục 7 |
| `discountAmount` | Số nguyên | Tiền giảm | DH BR-04, mục 7 |
| `vatRate` | Số thập phân | Thuế suất VAT đã áp dụng, ví dụ `0.1` | DH BR-04, mục 7 |
| `vatAmount` | Số nguyên | Tiền VAT | DH BR-04, mục 7 |
| `total` | Số nguyên | Tổng tiền | DH BR-04, mục 7 |
| `lines[]` | Mảng | `productId`, `productCode`, `productName`, `quantity`, `listPrice`, `unitPrice`, `lineAmount`, `priceChangeReason` | DH SCR-01, SCR-03, mục 7 (Dòng đơn hàng) |
| `createdAt` | String | ISO-8601 có offset `+07:00` | DH mục 7 (Ngày tạo); TD-11 |
| `createdBy` | Object | `id`, `fullName` | DH mục 7 (Người tạo); BD bảng `app_user` |

## 4. Mã lỗi
Body theo `ErrorResponse(code, message, fieldErrors)`. `fieldErrors` chỉ có ở lỗi 400 (BD mục 5, mã lỗi chung).

| HTTP | Mã | Khi nào | `fieldErrors` | Nguồn |
|------|----|---------|---------------|-------|
| 400 | `VALIDATION_ERROR` | Một trong các trường hợp sau:<br>(a) thiếu hoặc sai định dạng `Idempotency-Key`.<br>(b) JSON sai cú pháp hoặc sai kiểu (`quantity` = `1.5` hoặc `"abc"`).<br>(c) vi phạm ràng buộc mục 2: `customerId` null, `lines` rỗng, `productId` null, `quantity` < 1, `unitPrice` âm, có phần lẻ hoặc quá 13 chữ số, lý do quá 200 ký tự, trùng `productId` | Khóa là tên trường (`Idempotency-Key`, `customerId`, `lines`, `lines[0].quantity`, `lines[1].priceChangeReason`…), giá trị là thông báo. JSON sai cú pháp: rỗng | BD mã lỗi chung; DH UC-01 E1, E2, E6; QA: Q-65, Q-75; TD-14, TD-16 |
| 401 | `UNAUTHORIZED` | Chưa đăng nhập, hết phiên; tài khoản bị vô hiệu hóa | — | BD mã lỗi chung; QA: Q-61, Q-64; TD-01, TD-15 |
| 403 | `CSRF_TOKEN_INVALID` | Thiếu header `X-XSRF-TOKEN` hoặc giá trị không khớp | — | BD mã lỗi chung; TD-01, TD-15 |
| 403 | `PASSWORD_CHANGE_REQUIRED` | Tài khoản đang phải đổi mật khẩu | — | BD mã lỗi chung; QA: Q-60, Q-63; TD-15 |
| 403 | `ORDER_PRICE_CHANGE_FORBIDDEN` | Nhân viên gửi `unitPrice` khác giá niêm yết ở ít nhất một dòng, kể cả khi có lý do | — | DH BR-03, UC-01 E5; BD mã lỗi chung; TD-15 |
| 404 | `NOT_FOUND` | Khách hàng hoặc sản phẩm không tồn tại; message nêu loại và id | — | DH UC-01 E4; BD mã lỗi chung |
| 409 | `ORDER_PRICE_REASON_REQUIRED` | Quản lý gửi `unitPrice` khác giá niêm yết mà lý do rỗng hoặc chỉ gồm khoảng trắng; message nêu mã sản phẩm của các dòng thiếu lý do | — | DH BR-03, UC-01 E6; CLAUDE.md (lỗi nghiệp vụ → 409) |
| 409 | `ORDER_INSUFFICIENT_STOCK` | Ít nhất một sản phẩm có tồn < số lượng đặt; message liệt kê mọi sản phẩm thiếu: mã sản phẩm, số đặt, tồn còn | — | DH BR-12, UC-01 E3, SCR-01 (số lượng không vượt tồn kho) |
| 409 | `CONCURRENT_UPDATE` | Chờ khóa dữ liệu quá 5 giây (bước 7, 8, 9, 18) | — | BD mã lỗi chung, mục 8; TD-04 |

Lỗi 403 `ACCESS_DENIED` (BD mã lỗi chung) không xảy ra ở API này, vì cả hai vai trò đều được tạo đơn (DH BR-16). API này tạo đơn mới nên không có trường hợp lệch `version` (TD-05).

## 5. Xử lý (theo thứ tự kiểm tra)

Lỗi ở bước nhỏ hơn được trả về trước; bước nào lỗi thì dừng ở bước đó.

| # | Kiểm tra / xử lý | Lỗi trả về | Nguồn |
|---|------------------|------------|-------|
| 1 | Tầng bảo mật: CSRF token hợp lệ. Bộ lọc CSRF của Spring Security chạy trước bước xác thực, nên request chưa đăng nhập mà thiếu token cũng nhận lỗi này | 403 `CSRF_TOKEN_INVALID` | TD-01, TD-15; BD mã lỗi chung |
| 2 | Tầng bảo mật: đã đăng nhập (phiên còn hiệu lực), tài khoản `enabled` | 401 `UNAUTHORIZED` | BD API-AUTH-01; QA: Q-61, Q-64; TD-01, TD-15 |
| 3 | Tầng bảo mật: tài khoản không ở trạng thái phải đổi mật khẩu | 403 `PASSWORD_CHANGE_REQUIRED` | BD API-AUTH-01; QA: Q-60, Q-63; TD-15 |
| 4 | Đọc header `Idempotency-Key`: có và đúng định dạng UUID. Tham số header khai báo trước body trong controller để lỗi header được trả trước | 400 `VALIDATION_ERROR` | TD-14 |
| 5 | Đọc JSON body: đúng cú pháp, đúng kiểu từng trường | 400 `VALIDATION_ERROR` | DH BR-02; BD mục 5 |
| 6 | Bean Validation trên body (mục 2), gồm constraint không trùng `productId` và độ dài lý do. Trả mọi lỗi trường trong một response | 400 `VALIDATION_ERROR` | DH BR-01, BR-02, BR-03; QA: Q-65, Q-75; TD-16 |
| — | **Bắt đầu transaction** (bước 7 → 20, mục 6) | | DH mục 8; BD mục 8 |
| 7 | `SET LOCAL lock_timeout = '5s'`. Khóa theo key (`pg_advisory_xact_lock`). Nếu đã có đơn mang `idempotency_key` này: trả 200 với đơn đó, kết thúc | 200; 409 `CONCURRENT_UPDATE` | TD-04, TD-14 |
| 8 | Đọc khách hàng theo `customerId`, khóa `FOR SHARE` | 404 `NOT_FOUND`; 409 `CONCURRENT_UPDATE` | DH UC-01 E4; BD mục 8; QA: Q-05 |
| 9 | Đọc các sản phẩm theo danh sách `productId`, khóa `FOR UPDATE`, sắp theo `id` tăng dần. Thiếu sản phẩm nào thì nêu id sản phẩm đó | 404 `NOT_FOUND`; 409 `CONCURRENT_UPDATE` | DH UC-01 E4; TD-04 |
| 10 | Từng dòng: `listPrice` = `product.price` (lúc lưu); `unitPrice` = giá trong request, không có thì = `listPrice`; `priceChanged` = `unitPrice.compareTo(listPrice) != 0` | — | DH BR-03, BR-25; QA: Q-66, Q-69 |
| 11 | **Quyền sửa giá:** người dùng là Nhân viên và có ít nhất một dòng `priceChanged` | 403 `ORDER_PRICE_CHANGE_FORBIDDEN` | DH BR-03, UC-01 E5; TD-15 |
| 12 | **Lý do sửa giá:** có dòng `priceChanged` mà lý do rỗng hoặc chỉ gồm khoảng trắng. Dòng không đổi giá thì lý do lưu null | 409 `ORDER_PRICE_REASON_REQUIRED` | DH BR-03, UC-01 E6, mục 7 |
| 13 | **Tồn kho:** từng dòng `stock ≥ quantity`. Gom mọi dòng thiếu vào một lỗi | 409 `ORDER_INSUFFICIENT_STOCK` | DH BR-12, UC-01 E3 |
| 14 | **Xét VIP:** tổng (`subtotal − discount_amount`) các đơn `DELIVERED` của khách ≥ `vip-threshold` | — | DH BR-06, BR-25; QA: Q-68; TD-08, TD-09 |
| 15 | **Tính tiền** bằng `PricingCalculator` (bảng dưới) với `vip-discount-rate`, `vat-rate` lúc lưu | — | DH BR-04, BR-05, BR-25; QA: Q-72, Q-73; TD-02 |
| 16 | **Trạng thái:** `subtotal − discountAmount > approval-threshold` → `PENDING_APPROVAL`, ngược lại `NEW` | — | DH BR-07, BR-08; QA: Q-68 |
| 17 | **Trừ tồn** từng sản phẩm trên entity đã khóa ở bước 9 | — | DH BR-12; TD-04 |
| 18 | **Sinh mã đơn** (bảng dưới) | 409 `CONCURRENT_UPDATE` | DH BR-21; QA: Q-46; TD-03, TD-11 |
| 19 | **Lưu** `sales_order` (gồm `vip`, `idempotency_key`, `version` = 0, `created_at`, `created_by`) và các `order_line` | — | DH mục 7; BD mục 6; TD-05 |
| 20 | **Ghi nhật ký** (mục 8) | — | DH BR-19; QA: Q-74; TD-06 |
| — | **Commit** | | |
| 21 | Trả 201 + `OrderResponse` | 201 | Mục 3 |

Bước 11 và 12 là hai lượt riêng trên toàn bộ các dòng. Nếu kiểm tra từng dòng một, lỗi 409 ở dòng 1 có thể che lỗi 403 ở dòng 2. Quyền phải được kiểm tra trước nghiệp vụ.

### Công thức tính tiền
`PricingCalculator` là hàm thuần, dùng chung cho API-DH-04, API-DH-05, API-DH-12 (TD-02).

| Đại lượng | Công thức | Nguồn |
|-----------|-----------|-------|
| `lineAmount` | `unitPrice × quantity` (số nguyên, không cần làm tròn) | DH BR-04 |
| `subtotal` | Σ `lineAmount` | DH BR-04 |
| `discountRate` | VIP ? `vip-discount-rate` : `0` | DH BR-05 |
| `discountAmount` | `(subtotal × discountRate).setScale(0, RoundingMode.HALF_UP)`, tính **một lần trên cả đơn** | DH BR-04, BR-05; QA: Q-72, Q-73 |
| `vatAmount` | `((subtotal − discountAmount) × vatRate).setScale(0, RoundingMode.HALF_UP)` | DH BR-04; QA: Q-73 |
| `total` | `subtotal − discountAmount + vatAmount` | DH BR-04 |

Ví dụ (khách VIP, giảm 10%, VAT 10%): tạm tính 1.234.565 → giảm 123.456,5 → **123.457** → sau giảm 1.111.108 → VAT 111.110,8 → **111.111** → tổng **1.222.219**.

### Sinh mã đơn
- `orderDate` = `LocalDate.now(clock)`, với `clock` theo vùng `Asia/Ho_Chi_Minh` (TD-11).
- `seq` = kết quả câu upsert ở mục 7 (TD-03).
- `code` = `"PBH-" + orderDate định dạng yyyyMMdd + "-" + String.format("%03d", seq)`. Từ 1000 trở đi, `%03d` tự ra 4 chữ số (DH BR-21).

### Lưu ý khi viết code
- So sánh `BigDecimal` bằng `compareTo`, không dùng `equals`. Giá đọc từ cột `NUMERIC(15,2)` có scale 2: `5000.00.equals(5000)` trả `false` (TD-10).
- `product.price` luôn là số nguyên nhờ CHECK ở bảng `product` (BD mục 6; QA: Q-69). Chuyển sang giá niêm yết bằng `setScale(0, RoundingMode.UNNECESSARY)`.
- Giá trị trên SCR-DH-01 (lấy từ API-DH-12) chỉ để tham khảo. API luôn tính lại theo giá trị tại lúc lưu và không báo lỗi nếu khác (DH BR-25; QA: Q-66).

## 6. Transaction

**Phạm vi:** `OrderService.create` có `@Transactional`, bao trọn bước 7 → 20. Isolation mặc định của PostgreSQL (READ COMMITTED). Mọi lỗi trong phạm vi này đều rollback toàn bộ: tồn kho, số thứ tự mã đơn, đơn, dòng, nhật ký. Số thứ tự bị rollback không tính là "đã cấp" theo BR-21, vì đơn không được tạo.

**Dữ liệu bị khóa** (theo thứ tự dùng chung, BD mục 8):

| Thứ tự | Dữ liệu | Kiểu khóa | Bước | Giữ đến | Nguồn |
|--------|---------|-----------|------|---------|-------|
| 1 | `Idempotency-Key` | Advisory lock giao dịch (`pg_advisory_xact_lock`) | 7 | Commit hoặc rollback | TD-14 |
| 2 | Dòng `customer` | `FOR SHARE` | 8 | Commit hoặc rollback | BD mục 8; QA: Q-05 |
| 3 | Các dòng `product` trong đơn | `FOR UPDATE`, theo `id` tăng dần | 9 | Commit hoặc rollback | TD-04 |
| 4 | Dòng `order_code_counter` của ngày | Khóa dòng do câu upsert | 18 | Commit hoặc rollback | TD-03 |

Chờ khóa tối đa 5 giây (`SET LOCAL lock_timeout = '5s'` ở bước 7). Quá thời gian thì PostgreSQL báo lỗi, map sang 409 `CONCURRENT_UPDATE` (TD-04; BD mục 5).

Đơn mới chưa tồn tại nên không có kiểm tra `version` (TD-05 chỉ áp dụng cho API-DH-05, API-DH-06).

**Khi 2 người thao tác cùng lúc:**

| Tình huống | Hệ thống xử lý | Kết quả | TC |
|------------|----------------|---------|----|
| Hai người tạo đơn cùng một sản phẩm, tổng số lượng vượt tồn | Request sau chờ khóa sản phẩm (bước 9); khi request trước commit, đọc tồn đã bị trừ | Request sau nhận 409 `ORDER_INSUFFICIENT_STOCK`; tồn không âm | TC-32 |
| Hai đơn có cùng các sản phẩm A, B nhưng gửi theo thứ tự khác nhau | Cả hai khóa theo `id` tăng dần | Không deadlock; xử lý lần lượt | TC-33 |
| Cùng `Idempotency-Key` gửi hai lần song song | Request sau chờ advisory lock (bước 7); khi request trước commit, thấy đơn đã có | Một request nhận 201, request kia nhận 200 với cùng đơn. Nếu request trước rollback thì request sau xử lý như request mới | TC-31 |
| Nhiều người tạo đơn trong cùng ngày | Xếp hàng trên dòng bộ đếm của ngày (bước 18) | Mã không trùng, liên tục | TC-34 |
| Người khác sửa giá sản phẩm (API-SP-04) trong lúc tạo đơn | API-SP-04 commit trước bước 9: đơn dùng giá mới. API-SP-04 đến sau bước 9: chờ đơn commit, đơn dùng giá đọc ở bước 9 | Đơn lưu theo giá tại lúc lưu, không báo lỗi (BR-25) | TC-13 |
| Người khác sửa tồn tay (API-SP-04) trong lúc tạo đơn | API-SP-04 chờ khóa sản phẩm. Việc không ghi đè phần tồn vừa bị đơn trừ thuộc detail design F-03 (BD mục 5, `ProductService.update`) | Ngoài phạm vi API này | — |
| Người khác xóa khách hàng (API-KH-05) trong lúc tạo đơn | API-KH-05 chờ khóa dòng khách (BD mục 8). Sau khi đơn commit, kiểm tra thấy khách đã có đơn nên trả 409. Nếu xóa xong trước bước 8 thì đơn nhận 404 | Không có đơn trỏ tới khách đã xóa. Test thuộc F-02 | — |
| Người khác hủy hoặc từ chối một đơn khác có cùng sản phẩm (API-DH-06) | Hai bên cùng khóa sản phẩm theo `id` tăng dần (BD mục 8) | Xử lý lần lượt; tồn được hoàn trước hoặc sau tùy bên nào khóa trước | TC-33 |
| Một transaction khác giữ khóa sản phẩm quá 5 giây | Bước 9 hết thời gian chờ | 409 `CONCURRENT_UPDATE`; không lưu gì | TC-35 |

## 7. DB
Khớp BD mục 6. API này không thêm bảng hay cột nào ngoài BD mục 6. Ba cột `sales_order.vip`, `sales_order.idempotency_key`, `sales_order.version` đã có trong BD v0.6.

| Bảng | Đọc | Ghi |
|------|-----|-----|
| `customer` | Khách theo id (`FOR SHARE`) | — |
| `product` | Sản phẩm theo danh sách id (`FOR UPDATE`) | `stock` |
| `sales_order` | Đơn theo `idempotency_key`; tổng xét VIP | Thêm 1 dòng |
| `order_line` | — | Thêm các dòng |
| `order_code_counter` | — | Upsert dòng của ngày |
| `audit_log` | — | Thêm 1 dòng |
| `app_user` | Người dùng hiện tại (qua module `auth`) | — |

Truy vấn chính:

```sql
-- Bước 7: gửi trùng
SELECT pg_advisory_xact_lock(hashtextextended(:idempotencyKey, 0));
SELECT id FROM sales_order WHERE idempotency_key = :idempotencyKey;

-- Bước 9: khóa sản phẩm (JPA: @Lock(PESSIMISTIC_WRITE), ORDER BY id)
SELECT * FROM product WHERE id IN (:ids) ORDER BY id FOR UPDATE;

-- Bước 14: xét VIP
SELECT COALESCE(SUM(subtotal - discount_amount), 0)
FROM sales_order
WHERE customer_id = :customerId AND status = 'DELIVERED';

-- Bước 18: số thứ tự mã đơn trong ngày
INSERT INTO order_code_counter (order_date, last_seq) VALUES (:orderDate, 1)
ON CONFLICT (order_date) DO UPDATE SET last_seq = order_code_counter.last_seq + 1
RETURNING last_seq;
```

Phác thảo migration cho phần API này dùng (Flyway, version tiếp theo sau migration của F-01, F-02; không sửa V1, V2):

```sql
CREATE TABLE sales_order (
    id               BIGSERIAL      PRIMARY KEY,
    code             VARCHAR(20)    NOT NULL UNIQUE,
    customer_id      BIGINT         NOT NULL REFERENCES customer (id),
    status           VARCHAR(20)    NOT NULL CHECK (status IN ('NEW', 'PENDING_APPROVAL', 'APPROVED',
                                         'REJECTED', 'DELIVERING', 'DELIVERED', 'CANCELLED')),
    vip              BOOLEAN        NOT NULL,
    subtotal         NUMERIC(15, 2) NOT NULL CHECK (subtotal >= 0 AND subtotal = trunc(subtotal)),
    discount_rate    NUMERIC(5, 4)  NOT NULL CHECK (discount_rate >= 0),
    discount_amount  NUMERIC(15, 2) NOT NULL CHECK (discount_amount >= 0 AND discount_amount = trunc(discount_amount)),
    vat_rate         NUMERIC(5, 4)  NOT NULL CHECK (vat_rate >= 0),
    vat_amount       NUMERIC(15, 2) NOT NULL CHECK (vat_amount >= 0 AND vat_amount = trunc(vat_amount)),
    total            NUMERIC(15, 2) NOT NULL CHECK (total >= 0 AND total = trunc(total)),
    idempotency_key  UUID           NOT NULL UNIQUE,
    version          BIGINT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ    NOT NULL,
    created_by       BIGINT         NOT NULL REFERENCES app_user (id),
    reviewed_at      TIMESTAMPTZ,
    reviewed_by      BIGINT         REFERENCES app_user (id),
    reject_reason    VARCHAR(200),
    cancelled_at     TIMESTAMPTZ,
    cancelled_by     BIGINT         REFERENCES app_user (id),
    cancel_reason    VARCHAR(200),
    delivered_at     TIMESTAMPTZ
);
CREATE INDEX idx_sales_order_created_at ON sales_order (created_at);
CREATE INDEX idx_sales_order_status ON sales_order (status);
CREATE INDEX idx_sales_order_customer_status ON sales_order (customer_id, status);

CREATE TABLE order_line (
    id                   BIGSERIAL      PRIMARY KEY,
    order_id             BIGINT         NOT NULL REFERENCES sales_order (id),
    product_id           BIGINT         NOT NULL REFERENCES product (id),
    quantity             INTEGER        NOT NULL CHECK (quantity >= 1),
    list_price           NUMERIC(15, 2) NOT NULL CHECK (list_price >= 0 AND list_price = trunc(list_price)),
    unit_price           NUMERIC(15, 2) NOT NULL CHECK (unit_price >= 0 AND unit_price = trunc(unit_price)),
    line_amount          NUMERIC(15, 2) NOT NULL CHECK (line_amount >= 0 AND line_amount = trunc(line_amount)),
    price_change_reason  VARCHAR(200),
    UNIQUE (order_id, product_id),
    CHECK (unit_price = list_price OR price_change_reason IS NOT NULL)
);

CREATE TABLE order_code_counter (
    order_date  DATE    PRIMARY KEY,
    last_seq    INTEGER NOT NULL
);
```

Ghi chú:
- Khóa ngoại `order_line.product_id` cũng chặn xóa sản phẩm đã có trong đơn (QA: Q-21), cùng với kiểm tra 409 ở API-SP-05.
- Bảng `audit_log` thuộc module `audit`, tạo ở migration của module đó (BD mục 6).

## 8. Log / Audit
Ghi đúng một dòng `audit_log` trong cùng transaction (DH BR-19; TD-06):

| Cột | Giá trị | Nguồn |
|-----|---------|-------|
| `occurred_at` | Bằng `sales_order.created_at` | DH BR-19 (thời điểm) |
| `user_id` | Người dùng hiện tại | DH BR-19 (người thao tác) |
| `action` | `ORDER_CREATE` (hiển thị "Tạo đơn") | DH BR-19 (loại thao tác) |
| `entity_type` / `entity_id` / `entity_code` | `ORDER` / id đơn / mã đơn | DH BR-19 (mã đối tượng của đơn là mã đơn); BD bảng `audit_log` |
| `before_data` | `NULL` (đơn chưa tồn tại) | DH BR-19 |
| `after_data` | JSONB gồm: `code`, `status`, `customer` (`id`, `name`, `phone`), `vip`, `subtotal`, `discountRate`, `discountAmount`, `vatRate`, `vatAmount`, `total`, `lines[]` (`productCode`, `quantity`, `listPrice`, `unitPrice`, `lineAmount`, `priceChangeReason`) | DH BR-19; QA: Q-74; TD-07 |

Không ghi bản ghi nhật ký riêng cho sản phẩm bị trừ tồn. Thay đổi tồn kho đã nằm trong `lines[]` của bản ghi đơn (QA: Q-74).

## 9. Thay đổi đi kèm và phụ thuộc

| Vị trí | Thay đổi | Nguồn |
|--------|----------|-------|
| Package `com.example.order.order` | `controller/OrderController`; `dto/CreateOrderRequest`, `OrderLineRequest`, `OrderResponse`, `OrderLineResponse`; `service/OrderService`, `PricingCalculator`, `OrderCodeGenerator`; `repository/SalesOrderRepository`, `OrderCodeCounterRepository`; `domain/SalesOrder` (có `@Version`), `OrderLine`, `OrderStatus` | CLAUDE.md (package theo chức năng); TD-05 |
| `ProductRepository` | Thêm hàm đọc theo danh sách id có khóa `PESSIMISTIC_WRITE`, `ORDER BY id` | TD-04 |
| `OrderProperties` (`@ConfigurationProperties("order")`) | `vip-discount-rate: 0.10`, `vat-rate: 0.10`, `approval-threshold: 50000000`, `vip-threshold: 200000000` | DH mục 7; QA: Q-27, Q-49, Q-68; TD-09 |
| `pom.xml`, cấu hình Spring Security, `GlobalExceptionHandler`, `ForbiddenException`, `application.yml` (Jackson), bean `Clock` | Theo BD mục 5, "Thay đổi trên code hiện có" | BD mục 5; TD-01, TD-11, TD-15 |

Phải có trước:
- F-01 (TD-01): Spring Security session + CSRF, người dùng hiện tại (id, vai trò), lỗi 401/403, bảng `app_user`.
- F-02: bảng `customer`.
- Module `audit`: `AuditService`, bảng `audit_log`.
- Thay đổi sản phẩm theo QA: Q-69: `ProductRequest.price` chỉ cho số nguyên; CHECK ở bảng `product`.
- Database test cho test tích hợp (TD-17).

## 10. Câu hỏi mở
Không có. Q-74 và Q-75 nảy sinh trong lúc thiết kế API này, nay đã đóng và đã áp dụng (mục 2, 7, 8).

## 11. Test cần có

Mức test:
- **Unit**: `PricingCalculator`, `OrderCodeGenerator`.
- **Service**: `OrderService` với Mockito, như `ProductServiceTest`.
- **Controller**: `@WebMvcTest`, như `ProductControllerTest`. Có Spring Security nên cần người dùng giả lập và CSRF token (BD mục 5).
- **Tích hợp**: PostgreSQL local, database riêng cho test (TD-17). Dùng cho khóa, upsert, advisory lock, CHECK.

Tên test theo quy ước `should_<kết quả>_when_<điều kiện>_BRxx` (CLAUDE.md).

| Mã | Tình huống / dữ liệu | Loại | Kết quả mong đợi | Mức | BR / mã lỗi | Tên test (gợi ý) |
|----|----------------------|------|------------------|-----|-------------|------------------|
| TC-01 | Nhân viên tạo đơn: khách thường, 2 dòng, không gửi `unitPrice`, tạm tính 1.000.000 | Bình thường | 201; `NEW`; mã `PBH-<ngày>-001`; `vip` = false; `version` = 0; tồn hai sản phẩm trừ đúng; 1 bản ghi nhật ký | Service | BR-01, BR-08, BR-12, BR-16, BR-19, BR-21 | `should_createNewOrder_when_staffSendsValidRequest_BR01` |
| TC-02 | Quản lý tạo đơn hợp lệ | Phân quyền | 201 | Controller | BR-16 | `should_return201_when_managerCreatesOrder_BR16` |
| TC-03 | Khách không VIP (tổng đã giao 0) | Bình thường | `discountRate` = 0, `discountAmount` = 0, `vip` = false, `total` = tạm tính + VAT | Unit | BR-05, BR-06 | `should_notDiscount_when_customerIsNotVip_BR05` |
| TC-04 | Khách VIP, tạm tính 1.234.565, giảm 10%, VAT 10% | Bình thường | Giảm 123.457; VAT 111.111; tổng 1.222.219; `vip` = true | Unit | BR-04, BR-05 | `should_applyVipDiscountHalfUp_when_customerIsVip_BR04` |
| TC-05 | (a) VIP, tạm tính 1.234.564 → giảm 123.456,4. (b) Không VIP, tạm tính 1.111.105 → VAT 111.110,5 | Biên | (a) giảm 123.456 (làm tròn xuống). (b) VAT 111.111 (0,5 làm tròn lên, không phải HALF_EVEN) | Unit | BR-04 | `should_roundHalfUp_when_fractionIsHalf_BR04` |
| TC-06 | Sau giảm, trước VAT = 50.000.000 / 50.000.001 | Biên | `NEW` / `PENDING_APPROVAL` | Unit | BR-07, BR-08 | `should_requireApproval_when_amountExceedsThreshold_BR07` |
| TC-07 | Tổng đã giao của khách = 199.999.999 / 200.000.000 | Biên | Không VIP / VIP | Service | BR-06 | `should_beVip_when_deliveredTotalReachesThreshold_BR06` |
| TC-08 | Khách có đơn `APPROVED` 300.000.000, không có đơn `DELIVERED` | Biên | Không VIP (chỉ tính đơn Đã giao) | Tích hợp | BR-06 | `should_countOnlyDeliveredOrders_when_checkingVip_BR06` |
| TC-09 | Cấu hình `vip-discount-rate` 0.05, `vat-rate` 0.08, `approval-threshold` 30.000.000; đơn VIP tạm tính 40.000.000 | Bình thường | Giảm 2.000.000; VAT 3.040.000; tổng 41.040.000; `PENDING_APPROVAL` (38.000.000 > 30.000.000) | Unit | BR-05, BR-07, BR-25 | `should_useConfiguredParameters_when_propertiesChanged_BR25` |
| TC-10 | Quản lý đổi giá: giá niêm yết 690.000, gửi 650.000, lý do "Khách quen" | Bình thường | 201; dòng lưu `listPrice` 690.000, `unitPrice` 650.000, lý do | Service | BR-03 | `should_saveChangedPrice_when_managerGivesReason_BR03` |
| TC-11 | (a) Sản phẩm giá niêm yết 0, Nhân viên không gửi `unitPrice`. (b) Quản lý gửi `unitPrice` 0 cho sản phẩm giá 5.000, có lý do | Biên | (a) 201, dòng 0 đồng, không cần lý do. (b) 201 | Service | BR-03 | `should_acceptZeroPrice_when_giftOrManagerReason_BR03` |
| TC-12 | Nhân viên gửi `unitPrice` 5000, giá niêm yết đọc từ DB là `5000.00` | Bình thường | 201; không coi là đổi giá; lý do lưu null | Service | BR-03 | `should_treatAsSamePrice_when_scaleDiffers_BR03` |
| TC-13 | FE hiển thị giá 5.000; giá sản phẩm đổi thành 6.000 trước khi lưu; request không gửi `unitPrice` | Bình thường | 201; `listPrice` = `unitPrice` = 6.000; không báo lỗi | Service | BR-25 | `should_useCurrentPrice_when_priceChangedBeforeSave_BR25` |
| TC-14 | Đặt số lượng = tồn (5/5) | Biên | 201; tồn còn 0 | Service | BR-12 | `should_create_when_quantityEqualsStock_BR12` |
| TC-15 | Bộ đếm trong ngày trả 999 / 1000 | Biên | Mã `PBH-<ngày>-999` / `PBH-<ngày>-1000` | Unit | BR-21 | `should_useFourDigits_when_sequenceReaches1000_BR21` |
| TC-16 | Tạo đơn lúc 2026-10-15T16:59:59Z và 2026-10-15T17:00:00Z (`Clock.fixed`) | Biên | `PBH-20261015-…` / `PBH-20261016-001` | Tích hợp | BR-21 | `should_resetSequence_when_vietnamDateChanges_BR21` |
| TC-17 | `customerId` null; `lines` = [] | Lỗi | 400 `VALIDATION_ERROR`; `fieldErrors` có `customerId` / `lines` | Controller | BR-01 | `should_return400_when_customerOrLinesMissing_BR01` |
| TC-18 | `quantity` = 0 / 1 / 1.5 / `"abc"` | Biên | 400 / hợp lệ / 400 / 400 | Controller | BR-02 | `should_return400_when_quantityInvalid_BR02` |
| TC-19 | `unitPrice` = -1 / 1000.5 / 10000000000000 (14 chữ số) / 9999999999999 (13 chữ số) | Biên | 400 / 400 / 400 / qua bước 6 | Controller | BR-03 | `should_return400_when_unitPriceInvalid_BR03` |
| TC-20 | Hai dòng cùng `productId` | Lỗi | 400; `fieldErrors` có `lines` | Controller | BR-02 | `should_return400_when_productDuplicated_BR02` |
| TC-21 | Thiếu `Idempotency-Key` / key = `"abc"` | Lỗi | 400; `fieldErrors` có `Idempotency-Key` | Controller | `VALIDATION_ERROR` (TD-14) | `should_return400_when_idempotencyKeyInvalid` |
| TC-22 | Lý do dài 200 / 201 ký tự | Biên | Hợp lệ / 400, `fieldErrors` có `lines[0].priceChangeReason` | Controller | BR-03 | `should_return400_when_reasonTooLong_BR03` |
| TC-23 | `customerId` không tồn tại; `productId` không tồn tại | Lỗi | 404 `NOT_FOUND`, message nêu loại và id | Service | `NOT_FOUND` (UC-01 E4) | `should_throwNotFound_when_customerOrProductMissing` |
| TC-24 | Quản lý đổi giá, lý do `""` / `"   "` | Lỗi | 409 `ORDER_PRICE_REASON_REQUIRED`; không lưu đơn; tồn không đổi | Service | BR-03 | `should_rejectCreate_when_priceChangedWithoutReason_BR03` |
| TC-25 | 2 sản phẩm, 1 thiếu tồn (đặt 6, tồn 5) | Lỗi | 409 `ORDER_INSUFFICIENT_STOCK`; message có mã sản phẩm, 6, 5; tồn cả hai không đổi; đơn tạo thành công ngay sau đó vẫn nhận số `001` | Tích hợp | BR-12, BR-21 | `should_rejectCreate_when_stockInsufficient_BR12` |
| TC-26 | Có CSRF token hợp lệ nhưng chưa đăng nhập; tài khoản bị vô hiệu hóa | Phân quyền | 401 `UNAUTHORIZED` | Controller | BR-16 | `should_return401_when_notAuthenticatedOrDisabled_BR16` |
| TC-27 | Tài khoản đang phải đổi mật khẩu | Phân quyền | 403 `PASSWORD_CHANGE_REQUIRED` | Controller | `PASSWORD_CHANGE_REQUIRED` (Q-60) | `should_return403_when_passwordChangeRequired` |
| TC-28 | Nhân viên gửi `unitPrice` khác giá niêm yết, có lý do | Phân quyền | 403 `ORDER_PRICE_CHANGE_FORBIDDEN`; không lưu | Service | BR-03, BR-16 | `should_forbid_when_staffChangesPrice_BR03` |
| TC-29 | Nhân viên: dòng 1 đổi giá không lý do, dòng 2 đổi giá có lý do | Phân quyền | 403 `ORDER_PRICE_CHANGE_FORBIDDEN` (không phải 409) | Service | BR-03 | `should_checkPermissionBeforeReason_when_multipleViolations_BR03` |
| TC-30 | Gửi lại cùng key sau khi lần đầu thành công | Đồng thời | 200; cùng id, cùng mã; tồn chỉ trừ một lần; bộ đếm không tăng | Tích hợp | TD-14 | `should_returnExistingOrder_when_idempotencyKeyReused` |
| TC-31 | Hai request cùng key chạy song song | Đồng thời | Chỉ 1 đơn; một nhận 201, một nhận 200 cùng mã | Tích hợp | TD-14 | `should_createOneOrder_when_sameKeySentConcurrently` |
| TC-32 | Hai đơn song song cùng sản phẩm tồn 5, mỗi đơn đặt 3 | Đồng thời | 1 đơn 201, 1 đơn 409 `ORDER_INSUFFICIENT_STOCK`; tồn còn 2 | Tích hợp | BR-12 | `should_neverOversell_when_ordersConcurrent_BR12` |
| TC-33 | Hai đơn song song: đơn 1 gửi sản phẩm (A, B), đơn 2 gửi (B, A) | Đồng thời | Cả hai hoàn tất; không deadlock | Tích hợp | TD-04 | `should_notDeadlock_when_productsSentInDifferentOrder` |
| TC-34 | 20 đơn song song trong cùng ngày | Đồng thời | Mã `001` → `020`, không trùng, không thiếu | Tích hợp | BR-21 | `should_generateUniqueCodes_when_ordersConcurrent_BR21` |
| TC-35 | Transaction khác giữ khóa một sản phẩm quá 5 giây | Đồng thời | 409 `CONCURRENT_UPDATE`; không lưu đơn; tồn không đổi | Tích hợp | `CONCURRENT_UPDATE` (TD-04) | `should_return409_when_lockTimeout` |
| TC-36 | Tạo đơn thành công | Nhật ký | 1 bản ghi `ORDER_CREATE`; `entity_type` `ORDER`; `entity_code` = mã đơn; `before_data` null; `after_data` đủ trường mục 8; không có bản ghi cho sản phẩm | Tích hợp | BR-19 | `should_writeOneOrderAudit_when_orderCreated_BR19` |
| TC-37 | Tạo đơn thất bại (409 thiếu tồn) | Nhật ký | Không có đơn, không có bản ghi nhật ký | Tích hợp | BR-19 | `should_notWriteAudit_when_createFails_BR19` |
| TC-38 | (a) Đã đăng nhập, thiếu header `X-XSRF-TOKEN`. (b) Đã đăng nhập, token sai. (c) Chưa đăng nhập và thiếu token | Phân quyền | (a), (b), (c) đều 403 `CSRF_TOKEN_INVALID`; không lưu. (c) cho thấy CSRF được kiểm tra trước xác thực | Controller | `CSRF_TOKEN_INVALID` (TD-01) | `should_return403_when_csrfTokenMissingOrInvalid` |

## 12. Bảng truy vết

| BR / Q / TD | Nội dung | Bước xử lý | Mã lỗi | TC |
|-------------|----------|------------|--------|----|
| BR-01 | Đúng 1 khách hàng, ít nhất 1 dòng | 6, 8 | `VALIDATION_ERROR`, `NOT_FOUND` | TC-01, TC-17, TC-23 |
| BR-02 | Số lượng nguyên ≥ 1; mỗi sản phẩm 1 dòng | 5, 6 | `VALIDATION_ERROR` | TC-18, TC-20 |
| BR-03 | Đơn giá ≥ 0, chỉ Quản lý đổi, đổi thì có lý do | 6, 10, 11, 12, 19 | `VALIDATION_ERROR`, `ORDER_PRICE_CHANGE_FORBIDDEN`, `ORDER_PRICE_REASON_REQUIRED` | TC-10, TC-11, TC-12, TC-19, TC-22, TC-24, TC-28, TC-29 |
| BR-04 | Công thức tiền, half-up từng bước | 15 | — | TC-04, TC-05 |
| BR-05 | Giảm VIP trên tạm tính cả đơn | 15 | — | TC-03, TC-04, TC-09 |
| BR-06 | Xét VIP theo đơn Đã giao | 14 | — | TC-03, TC-07, TC-08 |
| BR-07 | Ngưỡng duyệt | 16 | — | TC-06, TC-09 |
| BR-08 | Trạng thái ban đầu Mới / Chờ duyệt | 16 | — | TC-01, TC-06 |
| BR-12 | Đủ tồn, trừ tồn khi tạo | 13, 17 | `ORDER_INSUFFICIENT_STOCK` | TC-14, TC-25, TC-32 |
| BR-16 | Người dùng nội bộ, hai vai trò | 2, 11 | `UNAUTHORIZED`, `ORDER_PRICE_CHANGE_FORBIDDEN` | TC-01, TC-02, TC-26, TC-28 |
| BR-17 | Hiển thị tiền | — (FE) | — | — |
| BR-19 | Ghi nhật ký | 20 | — | TC-36, TC-37 |
| BR-21 | Mã đơn | 18 | `CONCURRENT_UPDATE` | TC-15, TC-16, TC-25, TC-34 |
| BR-25 | Giá trị tính tiền lấy lúc lưu | 9, 10, 14, 15, 16 | — | TC-09, TC-13 |
| Q-46 | Từ đơn thứ 1000 dùng 4 chữ số; ngày theo UTC+7 | 18 | — | TC-15, TC-16 |
| Q-60, Q-63 | Phải đổi mật khẩu | 3 | `PASSWORD_CHANGE_REQUIRED` | TC-27 |
| Q-61, Q-64 | Tài khoản bị vô hiệu hóa | 2 | `UNAUTHORIZED` | TC-26 |
| Q-65 | Đơn giá được bằng 0 | 6, 10 | `VALIDATION_ERROR` | TC-11, TC-19 |
| Q-66, Q-70 | Lưu theo giá lúc lưu, không báo riêng | 9, 10 | — | TC-13 |
| Q-68, Q-71 | Ngưỡng, tỷ lệ là tham số cấu hình | 14, 15, 16 | — | TC-09 |
| Q-69 | Giá sản phẩm là số nguyên | 10 | — | TC-12 |
| Q-72, Q-73 | Giảm một lần trên cả đơn, half-up | 15 | — | TC-04, TC-05 |
| Q-74 | Nhật ký chỉ ghi cho đơn | 20 | — | TC-36 |
| Q-75 | Lý do tối đa 200 ký tự | 6 | `VALIDATION_ERROR` | TC-22 |
| UC-01 E4 | Khách hàng / sản phẩm không tồn tại | 8, 9 | `NOT_FOUND` | TC-23 |
| TD-01 | Session + CSRF token | 1, 2 | `CSRF_TOKEN_INVALID`, `UNAUTHORIZED` | TC-26, TC-38 |
| TD-03 | Bộ đếm mã đơn | 18 | `CONCURRENT_UPDATE` | TC-34 |
| TD-04 | Khóa sản phẩm theo id, chờ tối đa 5 giây | 7, 8, 9, 17 | `CONCURRENT_UPDATE` | TC-32, TC-33, TC-35 |
| TD-05 | `version` của đơn | 19 | — | TC-01 |
| TD-14 | `Idempotency-Key` | 4, 7 | `VALIDATION_ERROR` | TC-21, TC-30, TC-31 |
| TD-15 | Mã lỗi 401/403 theo nguyên nhân | 1, 2, 3, 11 | `CSRF_TOKEN_INVALID`, `UNAUTHORIZED`, `PASSWORD_CHANGE_REQUIRED`, `ORDER_PRICE_CHANGE_FORBIDDEN` | TC-26, TC-27, TC-28, TC-38 |
| TD-16 | Không trùng sản phẩm trong request | 6 | `VALIDATION_ERROR` | TC-20 |
