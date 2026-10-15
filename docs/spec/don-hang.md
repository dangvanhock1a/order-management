# [DH] Đơn hàng
Phiên bản: 0.12 · Ngày: 2026-10-15 · Người viết: Dang Van Hoc

Nguồn:
- `input/yeu-cau.csv` (bản CSV của `input/yeu-cau.xlsx`). "Dòng N" là số dòng trong file CSV, dòng 1 là tiêu đề.
- `docs/spec/qa.md`: câu trả lời của khách cho Q-01 → Q-75 (Q-01 → Q-64 đóng ngày 2026-10-14; Q-65 → Q-75 đóng ngày 2026-10-15). Nguồn "QA: Q-xx" là câu trả lời của câu hỏi Q-xx.

Lịch sử thay đổi:
- 0.1: bản đầu, theo requirement và Q-01 → Q-25.
- 0.2: áp dụng câu trả lời Q-26 → Q-42. Thêm UC-07 → UC-09, BR-20 → BR-23, SCR-04, SCR-05.
- 0.3: áp dụng câu trả lời Q-43 → Q-49. Thêm BR-24; thêm lý do sửa giá (SCR-01) và phần lịch sử thao tác (SCR-03).
- 0.4: áp dụng câu trả lời Q-50 → Q-53. Sửa BR-03, BR-19; thêm thuộc tính giá niêm yết cho dòng đơn hàng.
- 0.5: áp dụng câu trả lời Q-54, Q-55. Sửa BR-03, BR-19. Không còn câu hỏi mở.
- 0.6: áp dụng câu trả lời Q-56, Q-57 (tài khoản người dùng, dữ liệu cho báo cáo). Ghi chú chờ Q-62 ở BR-22. Mở thêm Q-60 → Q-62.
- 0.7: áp dụng câu trả lời Q-60 → Q-64. Sửa BR-19 (nhật ký đổi mật khẩu), BR-22 (tìm đơn theo SĐT khách). Cập nhật mục 8 (tài khoản). Không còn câu hỏi mở.
- 0.8: áp dụng câu trả lời Q-65 → Q-68. Sửa BR-03 (đơn giá ≥ 0), BR-06 và BR-07 (ngưỡng VIP, ngưỡng duyệt là tham số cấu hình). Thêm BR-25 (giá trị tính tiền lấy tại thời điểm lưu đơn), UC-01 A2. Ghi rõ đơn không có địa chỉ giao, ghi chú (mục 2). Mở Q-69 → Q-71.
- 0.9: áp dụng câu trả lời Q-69 → Q-71. Giá sản phẩm là số nguyên đồng (BR-03, mục 2). BR-25 áp dụng cả khi sửa đơn; dòng thêm mới lấy giá lúc lưu đơn (BR-03, UC-02, SCR-01, mục 7). Đổi tham số không tính lại đơn đã lưu (BR-23, mục 7). Ghi chú chờ Q-72, Q-73 ở BR-04, BR-05. Mở Q-72, Q-73.
- 0.10: áp dụng câu trả lời Q-72, Q-73. Khách xác nhận lại cách tính ở BR-04, BR-05 (giảm giá một lần trên tạm tính cả đơn, làm tròn half-up); bỏ ghi chú chờ. Không còn câu hỏi mở.
- 0.11: áp dụng câu trả lời Q-74. Sửa BR-19: thay đổi tồn kho do đơn hàng chỉ ghi trong nhật ký của đơn. Cập nhật SCR-05, mục 7. Không còn câu hỏi mở.
- 0.12: áp dụng câu trả lời Q-75. Lý do sửa giá, lý do hủy, lý do từ chối tối đa 200 ký tự (BR-03, BR-09, BR-11, SCR-01, SCR-03, mục 7). Không còn câu hỏi mở.

## 1. Mục tiêu
Hệ thống phục vụ một cửa hàng bán hàng (Q-50). Cho phép nhân viên tạo và quản lý đơn hàng cho khách: tính tổng tiền có giảm giá VIP và VAT, trình quản lý duyệt đơn giá trị lớn, hủy đơn, sửa đơn, theo dõi giao hàng, tra cứu đơn và xem nhật ký thao tác (input/yeu-cau.csv dòng 4–6; docs/spec/qa.md).

## 2. Phạm vi
- Trong phạm vi:
  - Tạo đơn hàng: chọn khách, thêm sản phẩm, tính tổng tiền, giảm giá VIP (dòng 4).
  - Duyệt đơn trên 50 triệu (dòng 5) và từ chối đơn (Q-12).
  - Hủy đơn (dòng 6; Q-14, Q-32).
  - Sửa đơn (Q-17, Q-34).
  - Bắt đầu giao và đánh dấu đơn đã giao (Q-13, Q-31).
  - Kiểm tra, trừ và hoàn tồn kho theo đơn (Q-15, Q-36).
  - Danh sách và tra cứu đơn hàng (Q-38, Q-43).
  - Màn hình nhật ký thao tác chung cho Quản lý, gồm dữ liệu đơn hàng, khách hàng, sản phẩm (Q-42, Q-47); phần lịch sử thao tác trong chi tiết đơn (Q-47).
- Ngoài phạm vi:
  - Quản lý khách hàng (dòng 2), quản lý sản phẩm (dòng 3), báo cáo doanh thu (dòng 7): thuộc spec riêng. Báo cáo dùng dữ liệu đơn Đã giao theo ngày giao (Q-19), hiển thị doanh thu cả trước VAT và sau VAT (Q-57). Vì vậy đơn hàng phải lưu ngày giao, số tiền sau giảm giá trước VAT và tổng tiền sau VAT. Spec khách hàng và sản phẩm phải ghi nhật ký theo cùng định dạng BR-19 để hiển thị trên SCR-05. Spec sản phẩm phải quy định giá sản phẩm là số nguyên đồng, được bằng 0 (Q-65, Q-69).
  - Quản lý tài khoản người dùng: IT tạo tài khoản và gán vai trò bằng script khi triển khai; không có màn hình quản lý người dùng (Q-56). Đăng nhập và đổi mật khẩu là chức năng chung, xem basic design F-01.
  - Thông báo email/SMS (Q-24).
  - Khách hàng tự thao tác trên hệ thống (Q-01).
  - Màn hình cấu hình tỷ lệ giảm giá VIP, thuế suất VAT, ngưỡng duyệt và ngưỡng VIP (Q-27, Q-49, Q-68: đổi bằng cấu hình hệ thống).
  - Địa chỉ giao, người nhận và ghi chú trên đơn: không có; giao theo thông tin trên hồ sơ khách hoặc xử lý ngoài hệ thống (Q-67).

## 3. Tác nhân
| Tác nhân | Mô tả | Quyền |
|----------|-------|-------|
| Nhân viên | Người dùng nội bộ, thao tác thay cho khách hàng (Q-01, Q-02) | Tạo, sửa (trừ đơn giá), hủy đơn; bắt đầu giao, đánh dấu đã giao; tra cứu đơn. Xem và thao tác được mọi đơn, không giới hạn đơn do mình tạo (Q-44) |
| Quản lý | Người dùng nội bộ (dòng 5; Q-02) | Toàn bộ quyền của Nhân viên; sửa đơn giá (Q-37, Q-48); duyệt và từ chối đơn, kể cả đơn do chính mình tạo (Q-40); xem nhật ký thao tác, gồm màn hình chung và phần lịch sử trong chi tiết đơn (Q-42, Q-47, Q-52) |

## 4. Use case

### UC-01 Tạo đơn hàng
- Tác nhân: Nhân viên, Quản lý
- Tiền điều kiện: đã đăng nhập; khách hàng và sản phẩm đã tồn tại.
- Luồng chính:
  1. Người dùng mở SCR-01 và chọn một khách hàng.
  2. Người dùng thêm sản phẩm và nhập số lượng. Thêm trùng sản phẩm thì hệ thống gộp vào dòng đã có (BR-02).
  3. Hệ thống điền đơn giá bằng giá sản phẩm hiện tại. Chỉ Quản lý được sửa đơn giá từng dòng; đơn giá khác giá niêm yết thì phải nhập lý do (BR-03).
  4. Hệ thống xác định khách có phải VIP không tại thời điểm này (BR-06), rồi tính tạm tính, giảm giá VIP, VAT và tổng tiền (BR-04, BR-05).
  5. Người dùng xác nhận tạo đơn.
  6. Hệ thống kiểm tra tồn kho từng sản phẩm (BR-12).
  7. Hệ thống tính lại tiền theo giá trị tại thời điểm lưu (BR-25), sinh mã đơn (BR-21), lưu đơn, trừ tồn kho và đặt trạng thái (BR-08):
     - Tổng sau giảm giá, trước VAT > ngưỡng duyệt: Chờ duyệt (BR-07).
     - Ngược lại: Mới.
  8. Hệ thống ghi nhật ký (BR-19).
- Luồng thay thế:
  - A1. Khách không phải VIP: không có giảm giá, tổng = tạm tính + VAT.
  - A2. Giá sản phẩm, trạng thái VIP hoặc tham số hệ thống thay đổi sau khi màn hình hiển thị: hệ thống lưu theo giá trị tại thời điểm lưu, không báo riêng. Chi tiết đơn (SCR-03) hiển thị số đã lưu, kể cả khi trạng thái khác với thông báo đã hiển thị trên SCR-01 (BR-25).
- Ngoại lệ:
  - E1. Chưa chọn khách hàng hoặc không có dòng sản phẩm nào: báo lỗi, không lưu (BR-01).
  - E2. Số lượng không phải số nguyên ≥ 1: báo lỗi (BR-02).
  - E3. Một sản phẩm không đủ tồn kho: báo lỗi nghiệp vụ, không lưu, không trừ kho (BR-12).
  - E4. Khách hàng hoặc sản phẩm không tồn tại: báo lỗi không tìm thấy.
  - E5. Nhân viên gửi đơn giá khác giá sản phẩm: không có quyền (BR-03).
  - E6. Đơn giá không phải số nguyên ≥ 0, hoặc khác giá niêm yết mà không có lý do: báo lỗi (BR-03).
- Hậu điều kiện: đơn được lưu ở trạng thái Mới hoặc Chờ duyệt, có mã đơn, tồn kho đã trừ.
- Business rule liên quan: BR-01 → BR-08, BR-12, BR-16, BR-19, BR-21, BR-25

### UC-02 Sửa đơn hàng
- Tác nhân: Nhân viên, Quản lý
- Tiền điều kiện: đơn ở trạng thái Mới hoặc Chờ duyệt (BR-14). Người sửa không cần là người tạo đơn (BR-16).
- Luồng chính:
  1. Người dùng mở đơn (SCR-03) và chọn Sửa. Hệ thống mở SCR-01 với dữ liệu hiện tại.
  2. Người dùng sửa dòng sản phẩm hoặc số lượng. Dòng cũ giữ đơn giá và giá niêm yết đã chốt; dòng thêm mới lấy giá sản phẩm tại thời điểm lưu đơn (BR-03, BR-25). Quản lý được sửa thêm đơn giá, kèm lý do nếu khác giá niêm yết (BR-03). Không đổi được khách hàng (BR-14).
  3. Hệ thống xác định lại trạng thái VIP của khách, rồi tính lại tổng tiền theo tỷ lệ giảm giá và thuế suất hiện tại (BR-23, BR-04 → BR-06).
  4. Người dùng xác nhận lưu.
  5. Hệ thống tính lại tiền theo giá trị tại thời điểm lưu (BR-25) và điều chỉnh tồn kho theo phần chênh lệch số lượng (BR-20).
  6. Hệ thống đặt lại trạng thái theo tổng mới: > ngưỡng BR-07 thì Chờ duyệt, ngược lại thì Mới (BR-08).
  7. Hệ thống ghi nhật ký (BR-19).
- Luồng thay thế:
  - A1. Giá sản phẩm của dòng thêm mới, trạng thái VIP hoặc tham số hệ thống thay đổi sau khi màn hình hiển thị: hệ thống lưu theo giá trị tại thời điểm lưu, không báo riêng. Chi tiết đơn (SCR-03) hiển thị số đã lưu (BR-25).
- Ngoại lệ:
  - E1. Đơn không ở trạng thái Mới hoặc Chờ duyệt: báo lỗi nghiệp vụ.
  - E2. Dữ liệu vi phạm BR-01, BR-02: báo lỗi.
  - E3. Không đủ tồn kho cho phần số lượng tăng thêm: báo lỗi nghiệp vụ, không lưu (BR-20).
  - E4. Nhân viên sửa đơn giá hoặc người dùng đổi khách hàng: báo lỗi (BR-03, BR-14).
  - E5. Đơn giá vi phạm BR-03 (không phải số nguyên ≥ 0, hoặc thiếu lý do): báo lỗi.
- Hậu điều kiện: đơn được cập nhật, trạng thái Mới hoặc Chờ duyệt theo bước 6, tồn kho đã điều chỉnh.
- Business rule liên quan: BR-01 → BR-08, BR-14, BR-16, BR-19, BR-20, BR-23, BR-25

### UC-03 Duyệt đơn hàng
- Tác nhân: Quản lý
- Tiền điều kiện: đơn ở trạng thái Chờ duyệt.
- Luồng chính:
  1. Quản lý mở SCR-02. Màn hình hiện mọi đơn Chờ duyệt, không giới hạn ngày, đơn tạo trong ngày lên trên cùng (BR-24). Quản lý chọn một đơn.
  2. Quản lý xem chi tiết đơn (SCR-03) và chọn Duyệt.
  3. Hệ thống chuyển đơn sang Đã duyệt và ghi nhật ký (BR-19).
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Người dùng không phải Quản lý: không có quyền (BR-10).
  - E2. Đơn không ở trạng thái Chờ duyệt: báo lỗi nghiệp vụ.
- Hậu điều kiện: đơn ở trạng thái Đã duyệt. Từ đây đơn không còn được sửa hay hủy (BR-11, BR-14).
- Business rule liên quan: BR-07, BR-08, BR-10, BR-19, BR-24

### UC-04 Từ chối đơn hàng
- Tác nhân: Quản lý
- Tiền điều kiện: đơn ở trạng thái Chờ duyệt.
- Luồng chính:
  1. Quản lý mở chi tiết đơn (SCR-03) và chọn Từ chối.
  2. Quản lý nhập lý do từ chối (bắt buộc).
  3. Hệ thống chuyển đơn sang Từ chối, hoàn tồn kho và ghi nhật ký.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Không nhập lý do: báo lỗi (BR-09).
  - E2. Người dùng không phải Quản lý (BR-10), hoặc đơn không ở Chờ duyệt: báo lỗi.
- Hậu điều kiện: đơn ở trạng thái Từ chối, là trạng thái kết thúc. Tồn kho đã được hoàn.
- Business rule liên quan: BR-09, BR-10, BR-13, BR-19

### UC-05 Hủy đơn hàng
- Tác nhân: Nhân viên, Quản lý (thao tác thay khách: Q-01)
- Tiền điều kiện: đơn ở trạng thái Mới hoặc Chờ duyệt (BR-11).
- Luồng chính:
  1. Người dùng mở chi tiết đơn (SCR-03) và chọn Hủy.
  2. Người dùng nhập lý do hủy (bắt buộc) và xác nhận.
  3. Hệ thống chuyển đơn sang Đã hủy, hoàn tồn kho và ghi nhật ký.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Đơn không ở trạng thái Mới hoặc Chờ duyệt: báo lỗi nghiệp vụ (BR-11).
  - E2. Không nhập lý do hủy: báo lỗi (BR-11).
  - E3. Đơn không tồn tại: báo lỗi không tìm thấy.
- Hậu điều kiện: đơn ở trạng thái Đã hủy, tồn kho đã được hoàn.
- Business rule liên quan: BR-11, BR-13, BR-16, BR-19

### UC-06 Đánh dấu đơn đã giao
- Tác nhân: Nhân viên, Quản lý
- Tiền điều kiện: đơn ở trạng thái Đang giao.
- Luồng chính:
  1. Người dùng mở chi tiết đơn (SCR-03) và chọn Đánh dấu đã giao.
  2. Hệ thống chuyển đơn sang Đã giao, lưu ngày giao và ghi nhật ký.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Đơn không ở trạng thái Đang giao: báo lỗi nghiệp vụ.
- Hậu điều kiện: đơn ở trạng thái Đã giao. Đơn được tính vào doanh thu tháng của ngày giao (Q-19) và vào doanh số tích lũy xét VIP (BR-06).
- Business rule liên quan: BR-08, BR-15, BR-19

### UC-07 Bắt đầu giao đơn
- Tác nhân: Nhân viên, Quản lý
- Tiền điều kiện: đơn ở trạng thái Mới hoặc Đã duyệt.
- Luồng chính:
  1. Người dùng mở chi tiết đơn (SCR-03) và chọn Bắt đầu giao.
  2. Hệ thống chuyển đơn sang Đang giao và ghi nhật ký.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Đơn không ở trạng thái Mới hoặc Đã duyệt (ví dụ đơn Chờ duyệt chưa được duyệt): báo lỗi nghiệp vụ.
- Hậu điều kiện: đơn ở trạng thái Đang giao, không còn được sửa hay hủy.
- Business rule liên quan: BR-08, BR-15, BR-19

### UC-08 Tra cứu danh sách đơn hàng
- Tác nhân: Nhân viên, Quản lý (mọi người dùng xem được mọi đơn: BR-16)
- Tiền điều kiện: đã đăng nhập.
- Luồng chính:
  1. Người dùng mở SCR-04. Hệ thống hiển thị các đơn có ngày tạo là ngày hiện tại (BR-22).
  2. Người dùng tìm theo trạng thái, SĐT khách hàng, khoảng ngày tạo, mã đơn (BR-22).
  3. Hệ thống hiển thị kết quả có phân trang (BR-18).
  4. Người dùng chọn một đơn để mở SCR-03.
- Luồng thay thế: không có
- Ngoại lệ:
  - E1. Không có kết quả: hiển thị danh sách rỗng.
- Hậu điều kiện: không có
- Business rule liên quan: BR-16, BR-18, BR-22

### UC-09 Xem nhật ký thao tác
- Tác nhân: Quản lý
- Tiền điều kiện: đã đăng nhập với vai trò Quản lý.
- Luồng chính:
  1. Quản lý mở SCR-05.
  2. Quản lý lọc theo khoảng thời gian, người thao tác, loại đối tượng (đơn hàng, khách hàng, sản phẩm, tài khoản), mã đối tượng (khớp chính xác). Với khách hàng, mã đối tượng là số điện thoại hiện tại; với tài khoản là tên đăng nhập (BR-19).
  3. Hệ thống hiển thị các bản ghi nhật ký: người thao tác, thời điểm, loại thao tác, đối tượng, dữ liệu trước và sau.
- Luồng thay thế:
  - A1. Xem lịch sử của một đơn: Quản lý mở SCR-03, phần "Lịch sử thao tác" hiện nhật ký của riêng đơn đó (BR-19).
- Ngoại lệ:
  - E1. Người dùng không phải Quản lý mở SCR-05: không có quyền (BR-19).
  - E2. Nhân viên mở SCR-03: không thấy phần "Lịch sử thao tác" (BR-19).
- Hậu điều kiện: không có
- Business rule liên quan: BR-19

## 5. Business rule
| ID | Nội dung | Nguồn |
|----|----------|-------|
| BR-01 | Đơn hàng phải có đúng một khách hàng và tối thiểu một dòng sản phẩm. | input/yeu-cau.csv dòng 4 (STT 3); docs/spec/qa.md Q-18 (A) |
| BR-02 | Số lượng mỗi dòng là số nguyên ≥ 1. Thêm sản phẩm đã có trong đơn thì cộng số lượng vào dòng đó, không tạo dòng mới. | docs/spec/qa.md Q-18 (A) |
| BR-03 | Đơn giá lấy theo giá sản phẩm tại thời điểm tạo đơn và được chốt trên đơn (giá sản phẩm thay đổi sau đó không ảnh hưởng). Chỉ Quản lý được sửa đơn giá từng dòng; Nhân viên không được sửa. Đơn giá phải là số nguyên ≥ 0 đồng; đơn được phép có dòng đơn giá 0 (ví dụ hàng tặng). Đơn giá khác giá niêm yết thì bắt buộc nhập lý do cho dòng đó, tối đa 200 ký tự. "Giá niêm yết" là giá sản phẩm tại thời điểm tạo đơn và được lưu trên dòng. Nếu giá sản phẩm thay đổi sau đó mà Quản lý sửa đơn nhưng giữ nguyên đơn giá thì không cần lý do. Dòng thêm mới khi sửa đơn: đơn giá mặc định và giá niêm yết lấy theo giá sản phẩm tại thời điểm lưu đơn ("thời điểm thêm dòng" là lúc lưu đơn); các dòng cũ giữ giá đã chốt. Giá sản phẩm là số nguyên đồng, nên giá niêm yết luôn là số nguyên. | docs/spec/qa.md Q-16 (B); QA: Q-37, Q-48, Q-53, Q-55, Q-65, Q-69, Q-70, Q-75 |
| BR-04 | Tạm tính = Σ(đơn giá × số lượng). Giảm giá = làm tròn(Tạm tính × tỷ lệ BR-05). VAT = làm tròn((Tạm tính − Giảm giá) × thuế suất VAT), áp dụng chung cho mọi sản phẩm. Tổng tiền = Tạm tính − Giảm giá + VAT. "Làm tròn" là half-up đến đồng, làm ở từng bước. Không có phí vận chuyển. Thuế suất VAT là tham số cấu hình hệ thống, hiện là 10%. Khách đã xác nhận cách tính này thay cho câu "số lượng × đơn giá × % giảm giá … làm tròn lên" trong trả lời Q-69: không tính giảm giá theo từng dòng, không luôn làm tròn lên. | input/yeu-cau.csv dòng 4 (STT 3); docs/spec/qa.md Q-09 (B); QA: Q-28, Q-30, Q-49, Q-72, Q-73 |
| BR-05 | Khách VIP được giảm 10% trên tạm tính, giống nhau cho mọi khách VIP. Tỷ lệ đổi được bằng cấu hình hệ thống, không có màn hình cấu hình. Giảm một lần trên tạm tính của cả đơn, không tính theo từng dòng. | input/yeu-cau.csv dòng 4 (STT 3); docs/spec/qa.md Q-08 (A); QA: Q-27, Q-72 |
| BR-06 | Khách là VIP khi tổng giá trị (sau giảm giá, trước VAT) của các đơn Đã giao của khách, tính từ trước đến nay, ≥ ngưỡng VIP. Ngưỡng VIP là tham số cấu hình hệ thống, hiện là 200.000.000 VND. Trạng thái VIP được xét khi tạo đơn và khi sửa đơn (BR-23). | docs/spec/qa.md Q-07 (B); QA: Q-26, Q-41, Q-68 |
| BR-07 | Đơn có tổng sau giảm giá, trước VAT > ngưỡng duyệt phải được Quản lý duyệt. Bằng đúng ngưỡng thì không cần duyệt. Ngưỡng duyệt là tham số cấu hình hệ thống, hiện là 50.000.000 VND. | input/yeu-cau.csv dòng 5 (STT 4); docs/spec/qa.md Q-10 (A); QA: Q-29, Q-68 |
| BR-08 | Chuyển trạng thái đơn:<br>• Tạo hoặc sửa đơn: > ngưỡng BR-07 → Chờ duyệt; ngược lại → Mới.<br>• Chờ duyệt → Đã duyệt (Duyệt) hoặc Từ chối (Từ chối).<br>• Mới hoặc Đã duyệt → Đang giao (Bắt đầu giao).<br>• Đang giao → Đã giao (Đánh dấu đã giao).<br>• Mới hoặc Chờ duyệt → Đã hủy (Hủy).<br>Từ chối, Đã giao, Đã hủy là trạng thái kết thúc. | docs/spec/qa.md Q-11 (A); QA: Q-31, Q-32, Q-35 |
| BR-09 | Quản lý được từ chối đơn Chờ duyệt, bắt buộc nhập lý do, tối đa 200 ký tự. Từ chối là trạng thái kết thúc. | docs/spec/qa.md Q-12 (A); QA: Q-75 |
| BR-10 | Chỉ Quản lý được duyệt và từ chối đơn. Quản lý được duyệt đơn do chính mình tạo. | input/yeu-cau.csv dòng 5 (STT 4); docs/spec/qa.md Q-02 (A); QA: Q-40 |
| BR-11 | Đơn chỉ được hủy khi ở trạng thái Mới hoặc Chờ duyệt; từ Đã duyệt trở đi không hủy được. Bắt buộc nhập lý do hủy, tối đa 200 ký tự. Khách đã xác nhận quy tắc này thay cho câu "trước khi giao" trong requirement gốc. | input/yeu-cau.csv dòng 6 (STT 5); docs/spec/qa.md Q-14 (B); QA: Q-32, Q-33, Q-75 |
| BR-12 | Khi tạo đơn, mỗi sản phẩm phải đủ tồn kho cho số lượng đặt; không đủ thì chặn tạo đơn. Tồn kho bị trừ ngay khi tạo đơn. | docs/spec/qa.md Q-15 (A) |
| BR-13 | Đơn bị hủy hoặc bị từ chối thì hoàn lại tồn kho đã trừ. | docs/spec/qa.md Q-15 (A) |
| BR-14 | Chỉ được sửa đơn ở trạng thái Mới hoặc Chờ duyệt. Được sửa dòng sản phẩm, số lượng và đơn giá (đơn giá theo BR-03); không được đổi khách hàng. Sau khi sửa, trạng thái tính lại theo BR-08. | docs/spec/qa.md Q-17 (B); QA: Q-34, Q-35 |
| BR-15 | Nhân viên ghi nhận giao hàng bằng hai thao tác: "Bắt đầu giao" (Mới/Đã duyệt → Đang giao) và "Đánh dấu đã giao" (Đang giao → Đã giao). | docs/spec/qa.md Q-13 (A); QA: Q-31 |
| BR-16 | Chỉ người dùng nội bộ (Nhân viên, Quản lý) thao tác trên đơn. Khách muốn hủy thì nhân viên hủy thay. Mọi nhân viên xem và thao tác được mọi đơn, không giới hạn đơn do mình tạo (quyền riêng của Quản lý vẫn theo BR-03, BR-10). | docs/spec/qa.md Q-01 (A), Q-02 (A); QA: Q-44 |
| BR-17 | Số tiền tính bằng VND, hiển thị không có phần lẻ, phân cách hàng nghìn bằng dấu chấm. | docs/spec/qa.md Q-22 (A) |
| BR-18 | Danh sách phân trang 20 dòng/trang, bản ghi mới nhất lên trước. | docs/spec/qa.md Q-25 (A) |
| BR-19 | Mọi thao tác thay đổi dữ liệu đều được ghi nhật ký gồm người thao tác, thời điểm, loại thao tác, đối tượng, dữ liệu trước và sau. Nhật ký lưu vô thời hạn. Thao tác đổi mật khẩu cũng được ghi nhật ký với loại thao tác "Đổi mật khẩu", không lưu giá trị mật khẩu cũ hay mới. Quản lý xem trên một màn hình chung cho mọi dữ liệu (đơn hàng, khách hàng, sản phẩm, tài khoản), lọc theo khoảng thời gian, người thao tác, loại đối tượng, mã đối tượng. Mã đối tượng: đơn hàng là mã đơn, sản phẩm là mã sản phẩm, khách hàng là số điện thoại, tài khoản là tên đăng nhập; khớp chính xác. Tìm theo SĐT hiện tại của khách ra toàn bộ nhật ký của khách đó, kể cả các bản ghi trước khi khách đổi SĐT; tìm theo SĐT cũ không ra kết quả. Chi tiết đơn có phần lịch sử thao tác của riêng đơn đó, chỉ Quản lý xem được. Thay đổi tồn kho do tạo, sửa, hủy, từ chối đơn chỉ được ghi trong bản ghi nhật ký của đơn (dữ liệu của đơn gồm các dòng và số lượng); không ghi bản ghi riêng cho sản phẩm, nên lọc theo mã sản phẩm không ra các lần trừ/hoàn tồn theo đơn. | docs/spec/qa.md Q-23 (B); QA: Q-42, Q-44, Q-47, Q-51, Q-52, Q-54, Q-60, Q-74 |
| BR-20 | Khi sửa đơn, tồn kho được điều chỉnh theo chênh lệch: tăng số lượng hoặc thêm sản phẩm thì trừ thêm, giảm số lượng hoặc bỏ sản phẩm thì hoàn lại. Không đủ tồn cho phần tăng thêm thì chặn lưu. | QA: Q-36 |
| BR-21 | Mã đơn có dạng PBH-YYYYMMDD-NNN. YYYYMMDD là ngày tạo theo giờ Việt Nam (UTC+7). NNN là số thứ tự trong ngày, đánh lại từ 001 mỗi ngày; từ đơn thứ 1000 trong ngày dùng 4 chữ số (PBH-YYYYMMDD-1000). Số đã cấp không được dùng lại, kể cả khi đơn bị hủy. | QA: Q-39, Q-46 |
| BR-22 | Danh sách đơn mặc định hiển thị đơn có ngày tạo là ngày hiện tại. Người dùng tìm được theo: trạng thái; SĐT khách hàng (chứa một phần; không tìm theo tên khách); khoảng ngày tạo; mã đơn (chứa một phần). | QA: Q-38, Q-43, Q-59, Q-62 |
| BR-23 | Khi sửa đơn, hệ thống tính lại theo giá trị hiện tại: trạng thái VIP của khách, tỷ lệ giảm giá VIP, thuế suất VAT, ngưỡng duyệt và ngưỡng VIP. Đơn giá vẫn giữ theo BR-03. Khi tham số hệ thống thay đổi, đơn đã lưu không tự tính lại trạng thái hay số tiền; giá trị mới chỉ áp dụng cho lần tạo hoặc sửa đơn sau đó. | QA: Q-41, Q-71 |
| BR-24 | Danh sách đơn chờ duyệt hiển thị mọi đơn Chờ duyệt, không giới hạn ngày, đơn tạo trong ngày lên trên cùng (theo BR-18: mới nhất lên trước). | QA: Q-45 |
| BR-25 | Khi tạo hoặc sửa đơn, giá niêm yết (khi tạo đơn: mọi dòng; khi sửa đơn: dòng thêm mới), trạng thái VIP và các tham số (tỷ lệ giảm giá VIP, thuế suất VAT, ngưỡng duyệt, ngưỡng VIP) được lấy tại thời điểm lưu đơn. Nếu khác với số đang hiển thị trên màn hình thì hệ thống lưu theo giá trị tại thời điểm lưu, không báo riêng; chi tiết đơn hiển thị số đã lưu. | QA: Q-66, Q-68, Q-70 |

## 6. Màn hình

### SCR-01 Tạo / sửa đơn hàng
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Khách hàng | Chọn từ danh sách (tìm theo tên/SĐT) | Có | Khách hàng phải tồn tại. Khi sửa đơn: chỉ đọc | BR-01, BR-14 |
| Cờ VIP | Chỉ đọc | – | – | Theo BR-06. Là giá trị lúc hiển thị; khi lưu, hệ thống xét lại (BR-25) |
| Sản phẩm (từng dòng) | Chọn từ danh sách | Có, ≥ 1 dòng | Sản phẩm phải tồn tại; trùng thì gộp dòng | BR-01, BR-02 |
| Số lượng (từng dòng) | Số nguyên | Có | ≥ 1; không vượt tồn kho | BR-02, BR-12, BR-20 |
| Đơn giá (từng dòng) | Số nguyên (VND) | Có | Mặc định = giá sản phẩm (dòng thêm khi sửa đơn: giá tại thời điểm lưu đơn, BR-25). Nhân viên: chỉ đọc. Quản lý: được sửa, phải ≥ 0 | BR-03 |
| Lý do sửa giá (từng dòng) | Text | Có, khi đơn giá khác giá niêm yết của dòng (BR-03) | Không được rỗng; tối đa 200 ký tự | BR-03. Chỉ hiện với Quản lý |
| Thành tiền (từng dòng) | Số tiền | – | Tự tính = đơn giá × số lượng | Chỉ đọc |
| Tạm tính | Số tiền | – | Tự tính | BR-04 |
| Giảm giá VIP (10%) | Số tiền | – | Tự tính, làm tròn half-up | BR-04, BR-05 |
| VAT (theo tham số, hiện 10%) | Số tiền | – | Tự tính, làm tròn half-up | BR-04 |
| Tổng tiền | Số tiền | – | Tự tính | BR-04, BR-17 |
| Thông báo "Đơn cần quản lý duyệt" | Nhãn | – | Hiển thị khi tổng sau giảm giá, trước VAT > ngưỡng duyệt (hiện 50.000.000) | BR-07. Trạng thái thực tế xác định lúc lưu (BR-25) |

### SCR-02 Danh sách đơn chờ duyệt
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Mã đơn | Text | – | – | BR-21 |
| Khách hàng | Text | – | – | |
| Tổng tiền | Số tiền | – | – | BR-17 |
| Ngày tạo, người tạo | Ngày giờ / Text | – | – | |
| Phân trang | – | – | – | BR-18 |

Chỉ Quản lý truy cập được màn hình này (BR-10). Hiện mọi đơn Chờ duyệt, không giới hạn ngày, đơn tạo trong ngày lên trên cùng (BR-24).

### SCR-03 Chi tiết đơn hàng
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Thông tin đơn | Chỉ đọc | – | – | Như SCR-01, kèm mã đơn, trạng thái, lý do sửa giá từng dòng |
| Nút Sửa | Hành động | – | Chỉ hiện khi đơn Mới hoặc Chờ duyệt | BR-14 |
| Nút Hủy | Hành động | – | Chỉ hiện khi đơn Mới hoặc Chờ duyệt | BR-11 |
| Lý do hủy | Text | Có (khi hủy) | Không được rỗng; tối đa 200 ký tự | BR-11 |
| Nút Duyệt / Từ chối | Hành động | – | Chỉ Quản lý, đơn Chờ duyệt | BR-09, BR-10 |
| Lý do từ chối | Text | Có (khi từ chối) | Không được rỗng; tối đa 200 ký tự | BR-09 |
| Nút Bắt đầu giao | Hành động | – | Chỉ hiện khi đơn Mới hoặc Đã duyệt | BR-08, BR-15 |
| Nút Đánh dấu đã giao | Hành động | – | Chỉ hiện khi đơn Đang giao | BR-08, BR-15 |
| Lịch sử thao tác | Danh sách, chỉ đọc | – | Chỉ hiện với Quản lý | BR-19. Các cột như SCR-05, chỉ của đơn này |

### SCR-04 Danh sách đơn hàng
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Trạng thái | Chọn (các trạng thái BR-08) | Không | – | BR-22 |
| SĐT khách hàng | Text | Không | Chứa một phần | BR-22 |
| Từ ngày / Đến ngày (ngày tạo) | Ngày | Không | Mặc định = ngày hiện tại; Từ ngày ≤ Đến ngày | BR-22 |
| Mã đơn | Text | Không | Chứa một phần | BR-22 |
| Bảng kết quả | Danh sách | – | – | Mã đơn, khách hàng, ngày tạo, trạng thái, tổng tiền. Phân trang BR-18. Hiện mọi đơn thỏa điều kiện, không giới hạn người tạo (BR-16) |

### SCR-05 Nhật ký thao tác
| Trường | Kiểu | Bắt buộc | Validate | Ghi chú |
|--------|------|----------|----------|---------|
| Bộ lọc: Từ thời điểm / Đến thời điểm | Ngày giờ | Không | Từ ≤ Đến | BR-19 |
| Bộ lọc: Người thao tác | Chọn | Không | – | BR-19 |
| Bộ lọc: Loại đối tượng | Chọn: Đơn hàng, Khách hàng, Sản phẩm, Tài khoản | Không | – | BR-19 |
| Bộ lọc: Mã đối tượng | Text | Không | Khớp chính xác | BR-19. Đơn hàng: mã đơn; sản phẩm: mã sản phẩm; khách hàng: SĐT hiện tại; tài khoản: tên đăng nhập. Lọc theo mã sản phẩm không ra các lần trừ/hoàn tồn theo đơn |
| Người thao tác | Text | – | – | BR-19 |
| Thời điểm | Ngày giờ | – | – | BR-19 |
| Loại thao tác | Text | – | – | BR-19 |
| Loại đối tượng, mã đối tượng | Text | – | – | BR-19 |
| Dữ liệu trước / sau | Text | – | – | BR-19 |

Chỉ Quản lý truy cập được màn hình này (BR-19). Phân trang BR-18.

## 7. Dữ liệu
| Thực thể | Thuộc tính | Ràng buộc | Ghi chú |
|----------|------------|-----------|---------|
| Đơn hàng | Mã đơn, khách hàng, trạng thái, tạm tính, % giảm giá, tiền giảm giá, thuế suất VAT, tiền VAT, tổng tiền | Mã đơn duy nhất, không dùng lại (BR-21). Trạng thái ∈ {Mới, Chờ duyệt, Đã duyệt, Từ chối, Đang giao, Đã giao, Đã hủy} (BR-08). Số tiền ≥ 0, làm tròn đến đồng (BR-04) | % giảm giá và thuế suất lưu theo lần tính gần nhất (BR-23) |
| Đơn hàng | Ngày tạo, người tạo, người và thời điểm duyệt/từ chối, lý do từ chối, người và thời điểm hủy, lý do hủy, ngày giao | Lý do từ chối bắt buộc khi Từ chối (BR-09). Lý do hủy bắt buộc khi Hủy (BR-11). Mỗi lý do tối đa 200 ký tự (BR-09, BR-11) | Ngày tạo dùng cho mã đơn (UTC+7) và lọc danh sách (BR-21, BR-22). Ngày giao dùng cho báo cáo (Q-19) và xét VIP (BR-06) |
| Dòng đơn hàng | Đơn hàng, sản phẩm, số lượng, giá niêm yết, đơn giá, thành tiền, lý do sửa giá | Số lượng nguyên ≥ 1; một sản phẩm chỉ xuất hiện một lần trong đơn (BR-02). Đơn giá nguyên ≥ 0. Lý do sửa giá bắt buộc khi đơn giá ≠ giá niêm yết, tối đa 200 ký tự (BR-03) | Giá niêm yết = giá sản phẩm lúc tạo đơn (dòng thêm khi sửa đơn: lúc lưu đơn), là số nguyên, không đổi khi giá sản phẩm đổi (BR-03, BR-25) |
| Nhật ký thao tác | Người thao tác, thời điểm, loại thao tác, loại đối tượng, mã đối tượng, dữ liệu trước, dữ liệu sau | Lưu vô thời hạn (BR-19) | Dùng chung cho đơn hàng, khách hàng, sản phẩm, tài khoản (đổi mật khẩu, không lưu giá trị mật khẩu). Bản ghi của khách hàng phải gắn với chính khách hàng đó, không chỉ với SĐT, để tìm theo SĐT hiện tại ra được cả nhật ký trước khi đổi SĐT (BR-19). Thay đổi tồn kho do đơn hàng chỉ nằm trong bản ghi của đơn, không có bản ghi riêng cho sản phẩm (BR-19) |
| Tham số hệ thống | Tỷ lệ giảm giá VIP = 10%; thuế suất VAT = 10%; ngưỡng duyệt = 50.000.000 VND; ngưỡng VIP = 200.000.000 VND | Đổi bằng cấu hình hệ thống (BR-04 → BR-07) | Không có màn hình cấu hình. Giá trị mới chỉ áp dụng cho lần tạo hoặc sửa đơn sau khi đổi; đơn đã lưu không tự tính lại (BR-23) |

## 8. Yêu cầu phi chức năng
- Hiệu năng: requirement không nêu.
- Tài khoản:
  - IT tạo tài khoản và gán vai trò bằng script khi triển khai; người dùng tự đổi mật khẩu; quên mật khẩu thì liên hệ IT; không có màn hình quản lý người dùng (QA: Q-56).
  - Mật khẩu tối thiểu 8 ký tự, có cả chữ và số; mật khẩu mới phải khác mật khẩu hiện tại. Bắt buộc đổi mật khẩu ở lần đăng nhập đầu tiên và ở lần đăng nhập kế tiếp sau khi IT đặt lại mật khẩu. Đổi mật khẩu được ghi nhật ký, không lưu giá trị mật khẩu (QA: Q-60, Q-63; BR-19).
  - IT vô hiệu hóa tài khoản bằng script khi nhân viên nghỉ việc; tài khoản bị vô hiệu không đăng nhập được, dữ liệu cũ (đơn, nhật ký) giữ nguyên. Nếu đang đăng nhập thì phiên bị ngắt ở thao tác kế tiếp. Không tự khóa khi nhập sai mật khẩu (QA: Q-61, Q-64).
- Bảo mật / phân quyền: bắt buộc đăng nhập. Hai vai trò Nhân viên và Quản lý; mọi người dùng thao tác được mọi đơn (BR-16). Chỉ Quản lý được sửa đơn giá, duyệt/từ chối đơn, xem màn hình nhật ký và phần lịch sử trong chi tiết đơn (BR-03, BR-10, BR-19). Kiểm tra phân quyền ở backend, không chỉ ẩn nút.
- Log / audit: ghi nhật ký mọi thao tác thay đổi dữ liệu, gồm dữ liệu trước và sau, lưu vô thời hạn (BR-19).
- Tính nhất quán tồn kho: việc trừ và hoàn kho phải nằm cùng giao dịch với thao tác tạo/sửa/hủy/từ chối đơn (BR-12, BR-13, BR-20).
- Mã đơn: sinh số thứ tự trong ngày không bị trùng khi nhiều người tạo đơn cùng lúc; ngày tính theo UTC+7 (BR-21).

## 9. Câu hỏi mở
Xem docs/spec/qa.md:
- Đã đóng, đã áp dụng vào spec: Q-01, Q-02, Q-07 → Q-19, Q-22 → Q-57, Q-59 → Q-75. Q-58 thuộc báo cáo (basic design). Câu thứ hai trong trả lời Q-69 (công thức giảm giá, "làm tròn lên") không áp dụng; khách đã xác nhận lại cách tính ở Q-72, Q-73.
- Không còn câu hỏi mở.
