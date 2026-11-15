CREATE TABLE IF NOT EXISTS demo_hospital (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT NOT NULL
);

INSERT INTO demo_hospital (id, name, address)
VALUES (1, '샘플병원', '서울특별시 중구')
ON CONFLICT (id) DO NOTHING;
