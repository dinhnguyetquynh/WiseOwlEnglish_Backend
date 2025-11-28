-- 1. Thêm cột point_balance vào bảng learner_profiles
ALTER TABLE learner_profiles
    ADD COLUMN point_balance INTEGER NOT NULL DEFAULT 0;

-- 2. Tạo bảng category_stickers
CREATE TABLE category_stickers (
                                   id BIGSERIAL PRIMARY KEY,
                                   category_name VARCHAR(255) NOT NULL
);

-- 3. Tạo bảng stickers
CREATE TABLE stickers (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255),
                          image_url VARCHAR(500),
                          price INTEGER NOT NULL,
                          rarity VARCHAR(50),
                          category_id BIGINT,
                          CONSTRAINT fk_sticker_category FOREIGN KEY (category_id) REFERENCES category_stickers (id)
);

-- 4. Tạo bảng learner_stickers (bảng trung gian)
CREATE TABLE learner_stickers (
                                  id BIGSERIAL PRIMARY KEY,
                                  learner_id BIGINT NOT NULL,
                                  sticker_id BIGINT NOT NULL,
                                  purchased_at TIMESTAMP,
                                  CONSTRAINT fk_ls_learner FOREIGN KEY (learner_id) REFERENCES learner_profiles (id),
                                  CONSTRAINT fk_ls_sticker FOREIGN KEY (sticker_id) REFERENCES stickers (id)
);

-- 5. Dữ liệu mẫu (Bạn có thể sửa link ảnh)
INSERT INTO category_stickers (id, category_name) VALUES (1, 'Animals');
INSERT INTO category_stickers (id, category_name) VALUES (2, 'Anime');

INSERT INTO stickers (name, image_url, price, rarity, category_id) VALUES
                                                                       ('Sharky', 'https://res.cloudinary.com/dxhhluk84/image/upload/v1764252888/shark1_hq5jpw.jpg', 100, 'COMMON', 1),
                                                                       ('Frog', 'https://res.cloudinary.com/dxhhluk84/image/upload/v1764252889/frog1_joo6on.jpg', 200, 'COMMON', 1),
                                                                       ('Capybara', 'https://res.cloudinary.com/dxhhluk84/image/upload/v1764252889/capybara1_m9imtj.jpg', 100, 'EPIC', 1),
                                                                       ('Luffy', 'https://res.cloudinary.com/dxhhluk84/image/upload/v1764252888/luffy1_u9d6ew.jpg', 1000, 'COMMON', 2);