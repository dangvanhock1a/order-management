# [OM] Quản lý đơn hàng
Phiên bản: 0.1 (bản nháp, chờ khách trả lời Q&A) · Ngày: 2026-10-14 · Người viết: Dang Van Hoc

Nguồn: `input/yeu-cau.csv` (bản CSV của `input/yeu-cau.xlsx`). Các vị trí "dòng N" là số dòng trong file CSV (dòng 1 là tiêu đề, STT 1 nằm ở dòng 2).

## 1. Mục tiêu
Xây dựng hệ thống quản lý khách hàng, sản phẩm và đơn hàng gồm: tạo đơn có tính tổng tiền và giảm giá cho khách VIP, duyệt đơn giá trị lớn, hủy đơn, và báo cáo doanh thu theo tháng (input/yeu-cau.csv dòng 2–7).

## 2. Phạm vi
- Trong phạm vi (theo input/yeu-cau.csv):
  - Quản lý khách hàng: thêm, sửa, xóa; tìm theo tên, số điện thoại (dòng 2).
  - Quản lý sản phẩm: danh sách sản phẩm, giá, tồn kho (dòng 3).
  - Tạo đơn hàng: chọn khách hàng, thêm sản phẩm, tính tổng tiền, giảm giá cho khách VIP (dòng 4).
  - Duyệt đơn: đơn trên 50 triệu phải được quản lý duyệt (dòng 5).
  - Hủy đơn: khách được hủy đơn trước khi giao (dòng 6).
  - Báo cáo doanh thu theo tháng (dòng 7).
- Ngoài phạm vi: chưa xác định. Requirement không nêu các mục sau, đã ghi thành câu hỏi:
  - Chức năng giao hàng / cập nhật trạng thái giao (Q-13)
  - Sửa đơn hàng (Q-17)
  - Thông báo email/SMS (Q-24)
  - Xuất file báo cáo (Q-20)

## 3. Tác nhân
| Tác nhân | Mô tả | Quyền |
|----------|-------|-------|
| Quản lý | Được nêu ở input/yeu-cau.csv dòng 5 | Duyệt đơn trên 50 triệu. Các quyền khác chưa xác định (Q-02) |
| Khách hàng | Được nêu ở input/yeu-cau.csv dòng 6 | Hủy đơn trước khi giao. Chưa rõ khách có trực tiếp dùng hệ thống không (Q-01) |
| Người dùng thực hiện quản lý khách hàng / sản phẩm / tạo đơn / xem báo cáo | Requirement không nêu là ai | Chưa xác định (Q-01, Q-02) |

## 4. Use case

### UC-01 Thêm khách hàng
- Tác nhân: chưa xác định (Q-01, Q-02)
- Tiền điều kiện: người dùng đã truy cập hệ thống (cơ chế đăng nhập chưa xác định: Q-02)
- Luồng chính:
  1. Người dùng mở màn hình thêm khách hàng (SCR-02).
  2. Người dùng nhập thông tin khách hàng (tên, số điện thoại; các trường khác: Q-03).
  3. Hệ thống kiểm tra dữ liệu (quy tắc: Q-03, Q-04).
  4. Hệ thống lưu khách hàng mới.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Dữ liệu không hợp lệ hoặc số điện thoại bị trùng (nếu không được trùng: Q-04): hệ thống báo lỗi, không lưu.
- Hậu điều kiện: khách hàng mới có trong hệ thống.
- Business rule liên quan: BR-01

### UC-02 Sửa khách hàng
- Tác nhân: chưa xác định (Q-01, Q-02)
- Tiền điều kiện: khách hàng đã tồn tại.
- Luồng chính:
  1. Người dùng chọn khách hàng từ danh sách (SCR-01).
  2. Người dùng sửa thông tin trên SCR-02.
  3. Hệ thống kiểm tra dữ liệu (Q-03, Q-04) và lưu.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Khách hàng không tồn tại: báo lỗi không tìm thấy.
  - E2. Dữ liệu không hợp lệ: báo lỗi, không lưu.
- Hậu điều kiện: thông tin khách hàng được cập nhật. Chưa rõ việc đổi trạng thái VIP có ảnh hưởng đến đơn đã tạo không (Q-07, Q-16).
- Business rule liên quan: BR-01

### UC-03 Xóa khách hàng
- Tác nhân: chưa xác định (Q-01, Q-02)
- Tiền điều kiện: khách hàng đã tồn tại.
- Luồng chính:
  1. Người dùng chọn khách hàng cần xóa.
  2. Hệ thống yêu cầu xác nhận.
  3. Hệ thống xóa khách hàng.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Khách hàng không tồn tại: báo lỗi không tìm thấy.
  - E2. Khách hàng đã có đơn hàng: chưa xác định cách xử lý (Q-05).
- Hậu điều kiện: khách hàng không còn trong danh sách.
- Business rule liên quan: BR-01

### UC-04 Tìm kiếm khách hàng
- Tác nhân: chưa xác định (Q-01, Q-02)
- Tiền điều kiện: không có
- Luồng chính:
  1. Người dùng nhập tên hoặc số điện thoại trên SCR-01.
  2. Hệ thống trả về danh sách khách hàng phù hợp (cách so khớp: Q-06; phân trang: Q-25).
- Luồng thay thế:
  - A1. Không nhập điều kiện: chưa rõ có hiển thị toàn bộ danh sách không (Q-06).
- Ngoại lệ:
  - E1. Không có kết quả: hiển thị danh sách rỗng.
- Hậu điều kiện: không có
- Business rule liên quan: BR-02

### UC-05 Xem danh sách sản phẩm
- Tác nhân: chưa xác định (Q-01, Q-02)
- Tiền điều kiện: không có
- Luồng chính:
  1. Người dùng mở SCR-03.
  2. Hệ thống hiển thị danh sách sản phẩm kèm giá và tồn kho.
- Luồng thay thế: không có
- Ngoại lệ: không có
- Hậu điều kiện: không có
- Business rule liên quan: BR-03
- Ghi chú: code hiện có đã làm CRUD sản phẩm (mã, tên, giá, tồn kho). Requirement chỉ nêu "danh sách, giá, tồn kho". Cần khách xác nhận phạm vi thêm/sửa/xóa sản phẩm (Q-21).

### UC-06 Tạo đơn hàng
- Tác nhân: chưa xác định (Q-01, Q-02)
- Tiền điều kiện: đã có khách hàng và sản phẩm trong hệ thống.
- Luồng chính:
  1. Người dùng mở SCR-04, chọn khách hàng.
  2. Người dùng thêm sản phẩm vào đơn (ràng buộc số dòng, số lượng: Q-18).
  3. Hệ thống tính tổng tiền (công thức: Q-09; đơn giá áp dụng: Q-16).
  4. Nếu khách hàng là VIP, hệ thống áp dụng giảm giá (cách xác định VIP: Q-07; mức giảm: Q-08).
  5. Người dùng xác nhận tạo đơn.
  6. Hệ thống lưu đơn. Nếu đơn trên 50 triệu, đơn chuyển sang chờ quản lý duyệt (BR-07; ngưỡng: Q-10; trạng thái: Q-11).
- Luồng thay thế:
  - A1. Đơn không vượt ngưỡng 50 triệu: chưa xác định trạng thái tiếp theo của đơn (Q-11).
- Ngoại lệ:
  - E1. Khách hàng hoặc sản phẩm không tồn tại: báo lỗi không tìm thấy.
  - E2. Tồn kho không đủ: chưa xác định có chặn tạo đơn không (Q-15).
- Hậu điều kiện: đơn hàng được tạo. Ảnh hưởng tới tồn kho chưa xác định (Q-15).
- Business rule liên quan: BR-04, BR-05, BR-06, BR-07

### UC-07 Duyệt đơn hàng
- Tác nhân: Quản lý
- Tiền điều kiện: đơn hàng trên 50 triệu đang chờ duyệt.
- Luồng chính:
  1. Quản lý mở danh sách đơn chờ duyệt (SCR-05).
  2. Quản lý xem chi tiết đơn.
  3. Quản lý duyệt đơn.
  4. Hệ thống cập nhật trạng thái đơn (Q-11).
- Luồng thay thế:
  - A1. Quản lý từ chối đơn: chưa xác định có cho phép không và hệ quả ra sao (Q-12).
- Ngoại lệ:
  - E1. Người dùng không phải quản lý: không được duyệt (Q-02).
  - E2. Đơn không ở trạng thái chờ duyệt: báo lỗi nghiệp vụ.
- Hậu điều kiện: đơn được duyệt. Lưu vết người duyệt và thời điểm duyệt: Q-23.
- Business rule liên quan: BR-07

### UC-08 Hủy đơn hàng
- Tác nhân: Khách hàng (cách khách thao tác trên hệ thống: Q-01)
- Tiền điều kiện: đơn hàng chưa được giao.
- Luồng chính:
  1. Khách hàng (hoặc người thao tác thay khách, Q-01) chọn đơn cần hủy.
  2. Hệ thống kiểm tra đơn chưa giao (các trạng thái được phép hủy: Q-14).
  3. Hệ thống yêu cầu xác nhận (lý do hủy: Q-14).
  4. Hệ thống chuyển đơn sang trạng thái đã hủy.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Đơn đã giao: không cho hủy, báo lỗi nghiệp vụ.
  - E2. Đơn không tồn tại: báo lỗi không tìm thấy.
- Hậu điều kiện: đơn ở trạng thái đã hủy. Có hoàn tồn kho không: Q-15.
- Business rule liên quan: BR-08

### UC-09 Xem báo cáo doanh thu theo tháng
- Tác nhân: chưa xác định (Q-02)
- Tiền điều kiện: không có
- Luồng chính:
  1. Người dùng mở SCR-06 và chọn kỳ báo cáo (cách chọn: Q-20).
  2. Hệ thống tính doanh thu theo từng tháng (cách tính: Q-19).
  3. Hệ thống hiển thị kết quả.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Tháng không có doanh thu: hiển thị 0 hay ẩn tháng đó (Q-20).
- Hậu điều kiện: không có
- Business rule liên quan: BR-09

## 5. Business rule
| ID | Nội dung | Nguồn |
|----|----------|-------|
| BR-01 | Người dùng được thêm, sửa, xóa khách hàng. | input/yeu-cau.csv dòng 2 (STT 1 "Quản lý khách hàng") |
| BR-02 | Tìm khách hàng được theo tên và theo số điện thoại. | input/yeu-cau.csv dòng 2 (STT 1 "Quản lý khách hàng") |
| BR-03 | Danh sách sản phẩm hiển thị giá và tồn kho của từng sản phẩm. | input/yeu-cau.csv dòng 3 (STT 2 "Quản lý sản phẩm") |
| BR-04 | Khi tạo đơn hàng, người dùng phải chọn khách hàng và thêm sản phẩm vào đơn. | input/yeu-cau.csv dòng 4 (STT 3 "Tạo đơn hàng") |
| BR-05 | Hệ thống tự tính tổng tiền đơn hàng (công thức: Q-09). | input/yeu-cau.csv dòng 4 (STT 3 "Tạo đơn hàng") |
| BR-06 | Khách hàng VIP được giảm giá trên đơn hàng (cách xác định VIP và mức giảm: Q-07, Q-08). | input/yeu-cau.csv dòng 4 (STT 3 "Tạo đơn hàng") |
| BR-07 | Đơn hàng trên 50 triệu phải được quản lý duyệt (cách so ngưỡng: Q-10). | input/yeu-cau.csv dòng 5 (STT 4 "Duyệt đơn") |
| BR-08 | Khách hàng được hủy đơn khi đơn chưa được giao. | input/yeu-cau.csv dòng 6 (STT 5 "Hủy đơn") |
| BR-09 | Hệ thống cung cấp báo cáo doanh thu theo tháng (cách tính: Q-19). | input/yeu-cau.csv dòng 7 (STT 6 "Báo cáo") |

## 6. Màn hình
Bảng dưới chỉ liệt kê các trường có trong requirement. Các trường khác và quy tắc validate chờ khách trả lời.

### SCR-01 Danh sách / tìm kiếm khách hàng
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Từ khóa tên | Text | Không | Chưa xác định (Q-06) | BR-02 |
| Từ khóa số điện thoại | Text | Không | Chưa xác định (Q-06) | BR-02. Dùng 1 ô chung hay 2 ô riêng: Q-06 |
| Bảng kết quả | Danh sách | – | – | Các cột hiển thị: Q-03. Phân trang: Q-25 |

### SCR-02 Thêm / sửa khách hàng
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Tên | Text | Chưa xác định (Q-03) | Chưa xác định (Q-03) | |
| Số điện thoại | Text | Chưa xác định (Q-03) | Định dạng và tính duy nhất: Q-04 | |
| Trạng thái VIP | Chưa xác định | Chưa xác định | Chưa xác định | Có tồn tại dưới dạng trường nhập không: Q-07 |
| Các trường khác | – | – | – | Q-03 |

### SCR-03 Danh sách sản phẩm
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Sản phẩm | Text | – | – | Trường định danh (mã, tên) theo code hiện có: Q-21 |
| Giá | Số tiền | – | – | BR-03. Định dạng hiển thị: Q-22 |
| Tồn kho | Số | – | – | BR-03 |

### SCR-04 Tạo đơn hàng
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Khách hàng | Chọn từ danh sách | Có | Khách hàng phải tồn tại | BR-04 |
| Sản phẩm | Chọn từ danh sách | Có | Sản phẩm phải tồn tại. Số dòng tối thiểu và dòng trùng: Q-18 | BR-04 |
| Số lượng | Số | Chưa xác định | Chưa xác định (Q-18). Kiểm tra tồn kho: Q-15 | |
| Đơn giá | Số tiền | – | Chỉ đọc hay cho sửa: Q-16 | |
| Giảm giá VIP | Số tiền / % | – | Tự tính (Q-07, Q-08) | BR-06 |
| Tổng tiền | Số tiền | – | Tự tính (Q-09) | BR-05 |

### SCR-05 Duyệt đơn
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Danh sách đơn chờ duyệt | Danh sách | – | – | BR-07 |
| Nút Duyệt | Hành động | – | Chỉ Quản lý (Q-02) | |
| Nút Từ chối / Lý do | Hành động / Text | Chưa xác định | Chưa xác định | Q-12 |

### SCR-06 Báo cáo doanh thu theo tháng
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Kỳ báo cáo | Chưa xác định | Chưa xác định | Chưa xác định | Q-20 |
| Doanh thu từng tháng | Số tiền | – | – | BR-09. Cách tính: Q-19 |

## 7. Dữ liệu
| Thực thể | Thuộc tính | Ràng buộc | Ghi chú |
|----------|------------|-----------|---------|
| Khách hàng | Tên, số điện thoại, trạng thái VIP | Chưa xác định (Q-03, Q-04, Q-07) | Các thuộc tính khác: Q-03 |
| Sản phẩm | Giá, tồn kho | Code hiện có: mã duy nhất, giá ≥ 0, tồn ≥ 0 | Phạm vi: Q-21 |
| Đơn hàng | Khách hàng, tổng tiền, giảm giá, trạng thái | Trạng thái: Q-11 | Người/thời điểm duyệt, hủy: Q-23 |
| Dòng đơn hàng | Sản phẩm, số lượng, đơn giá | Q-16, Q-18 | |

## 8. Yêu cầu phi chức năng
- Hiệu năng: requirement không nêu.
- Bảo mật / phân quyền: chỉ Quản lý được duyệt đơn (BR-07). Các vai trò và quyền còn lại: Q-01, Q-02.
- Log / audit: requirement không nêu (Q-23).

## 9. Câu hỏi mở
Xem docs/spec/qa.md: Q-01 → Q-25.
