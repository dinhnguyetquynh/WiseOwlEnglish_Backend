-- Thêm cột deleted_at cho bảng test_question
ALTER TABLE test_question
    ADD COLUMN deleted_at TIMESTAMP NULL;

-- Thêm cột deleted_at cho bảng test_option
ALTER TABLE test_option
    ADD COLUMN deleted_at TIMESTAMP NULL;