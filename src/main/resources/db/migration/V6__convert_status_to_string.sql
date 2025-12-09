-- 1. Xóa Check Constraint cũ (nếu có) trên cột 'status'
DO $$
DECLARE
con_name text;
BEGIN
    -- Tìm tên constraint trên bảng 'lesson_progress' liên quan đến cột 'status'
SELECT con.conname INTO con_name
FROM pg_catalog.pg_constraint con
         INNER JOIN pg_catalog.pg_class rel ON rel.oid = con.conrelid
         INNER JOIN pg_catalog.pg_namespace nsp ON nsp.oid = connamespace
         INNER JOIN pg_catalog.pg_attribute attr ON attr.attrelid = rel.oid AND attr.attnum = ANY(con.conkey)
WHERE nsp.nspname = 'public'
  AND rel.relname = 'lesson_progress'
  AND attr.attname = 'status'
  AND con.contype = 'c'; -- 'c' là check constraint

-- Nếu tìm thấy thì xóa nó đi
IF con_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE lesson_progress DROP CONSTRAINT ' || quote_ident(con_name);
END IF;
END $$;

-- 2. Thay đổi kiểu dữ liệu và map giá trị
ALTER TABLE lesson_progress
ALTER COLUMN status TYPE VARCHAR(50)
USING CASE status
    WHEN 0 THEN 'NOT_STARTED'
    WHEN 1 THEN 'IN_PROGRESS'
    WHEN 2 THEN 'COMPLETED'
    ELSE 'NOT_STARTED'
END;