---
name: chuan-hoa-spec
description: Chuẩn hóa requirement thô (SRS, Excel, Word, biên bản họp, chat) thành spec Markdown theo template công ty kèm danh sách Q&A. Dùng khi nhận requirement mới hoặc requirement thay đổi.
argument-hint: [thư-mục-đầu-vào] [tên-chức-năng]
disable-model-invocation: true
allowed-tools: Read Grep Glob Write Bash(pandoc *)
---

Chuẩn hóa requirement trong thư mục $0 cho chức năng $1.

1. Đọc toàn bộ file trong $0. File .docx thì chuyển sang Markdown bằng pandoc trước khi đọc.
   File .xlsx: đọc bản .csv tương ứng; nếu chưa có thì yêu cầu người dùng lưu sang .csv.
2. Viết docs/spec/$1.md theo đúng template ${CLAUDE_SKILL_DIR}/spec-template.md.
3. Mỗi business rule đánh số BR-01, BR-02… và ghi nguồn: tên file + vị trí (dòng, sheet, mục).
4. QUAN TRỌNG: không tự giả định. Mọi điểm chưa rõ, mâu thuẫn hoặc thiếu ghi vào docs/spec/qa.md
   theo ${CLAUDE_SKILL_DIR}/qa-template.md, kèm mức ảnh hưởng (Cao/Trung bình/Thấp) và phương án đề xuất.
   Nếu qa.md đã có, thêm câu hỏi mới với ID tiếp theo, không xóa câu cũ.
5. Không thêm chức năng ngoài requirement. Nếu thấy cần, ghi vào Q&A dưới dạng câu hỏi.
6. Kết thúc: in tóm tắt số use case, số BR, số câu hỏi theo từng mức ảnh hưởng.
