-- users: admin/member 계정 (docs/architecture-requirements.md §6, §8)
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'member')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- listings: 매물 (특허/실용신안권, 상표권, 디자인권, 기술 라이센싱)
CREATE TABLE listings (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    category      VARCHAR(30) NOT NULL
                  CHECK (category IN ('특허/실용신안권', '상표권', '디자인권', '기술 라이센싱')),
    price         BIGINT,
    status        VARCHAR(20) NOT NULL DEFAULT '판매중'
                  CHECK (status IN ('판매중', '협의중', '판매완료')),
    app_number    VARCHAR(100),
    reg_number    VARCHAR(100),
    summary       VARCHAR(500),
    content       TEXT,
    thumbnail_url VARCHAR(500),
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by    BIGINT NOT NULL REFERENCES users (id)
);

CREATE INDEX idx_listings_deleted_at ON listings (deleted_at);
CREATE INDEX idx_listings_category ON listings (category);

-- listing_images: 매물 이미지 갤러리 (대표 1장 포함 최대 5장, 개수 제약은 애플리케이션에서 검증 - M6)
CREATE TABLE listing_images (
    id          BIGSERIAL PRIMARY KEY,
    listing_id  BIGINT NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    image_url   VARCHAR(500) NOT NULL,
    sort_order  SMALLINT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_listing_images_listing_id ON listing_images (listing_id);

-- inquiries: 매물 문의
CREATE TABLE inquiries (
    id         BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings (id),
    type       VARCHAR(20) NOT NULL CHECK (type IN ('매수', '라이센싱', '기타')),
    company    VARCHAR(255),
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(50),
    price_hope BIGINT,
    content    TEXT,
    agreed_at  TIMESTAMPTZ,
    status     VARCHAR(20) NOT NULL DEFAULT '신규'
               CHECK (status IN ('신규', '확인', '응답완료')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_inquiries_listing_id ON inquiries (listing_id);
