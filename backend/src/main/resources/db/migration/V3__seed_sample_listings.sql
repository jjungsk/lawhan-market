-- *** 개발용 샘플 데이터. 운영 배포 전 제거/검토 필요 (Public API 로컬 검증용) ***
--
-- listings 4건 (같은 카테고리 2건은 /related 검증용) + 이미지 일부 + soft-delete 1건.
-- created_by는 V2__seed_admin.sql에서 시딩한 admin 계정(id=1)을 참조한다.

INSERT INTO listings (id, title, category, price, status, app_number, reg_number, summary, content, thumbnail_url, created_at, updated_at, created_by)
VALUES
    (1, 'AI 이미지 압축 특허', '특허/실용신안권', 50000000, '판매중',
     '10-2023-0012345', '10-2456789',
     '딥러닝 기반 이미지 압축률 40% 개선 특허',
     'AI 기반 이미지 압축 기술로, 기존 대비 압축률을 유지하면서 화질 손실을 최소화한 특허입니다.',
     'https://placehold.co/600x400?text=Listing+1',
     now() - interval '2 day', now() - interval '2 day', 1),
    (2, '차세대 배터리 관리 시스템 특허', '특허/실용신안권', NULL, '협의중',
     '10-2023-0054321', NULL,
     '전기차용 BMS 열관리 알고리즘 특허, 가격 협의',
     '배터리 셀 간 온도 편차를 실시간으로 보정하는 BMS 제어 알고리즘 특허입니다.',
     'https://placehold.co/600x400?text=Listing+2',
     now() - interval '1 day', now() - interval '1 day', 1),
    (3, '프리미엄 화장품 브랜드 상표권', '상표권', 30000000, '판매중',
     NULL, '40-2022-9988776',
     '국내외 출원 완료된 프리미엄 화장품 브랜드 상표권',
     '국내 및 주요 5개국에 등록 완료된 화장품 브랜드 상표권입니다.',
     'https://placehold.co/600x400?text=Listing+3',
     now(), now(), 1),
    (4, '폴더블 힌지 디자인권', '디자인권', 20000000, '판매완료',
     NULL, '30-2021-0033445',
     '폴더블 디바이스용 힌지 구조 디자인권 (soft-delete 테스트용, 목록/상세에 노출되면 안 됨)',
     '내구성을 개선한 폴더블 힌지 구조 디자인권입니다.',
     'https://placehold.co/600x400?text=Listing+4',
     now() - interval '5 day', now() - interval '5 day', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO listing_images (listing_id, image_url, sort_order)
VALUES
    (1, 'https://placehold.co/800x600?text=Listing+1+Image+1', 0),
    (1, 'https://placehold.co/800x600?text=Listing+1+Image+2', 1),
    (3, 'https://placehold.co/800x600?text=Listing+3+Image+1', 0)
ON CONFLICT DO NOTHING;

-- 4번 매물을 soft delete 처리 (deleted_at 설정) — 목록/상세/related 어디서도 조회되면 안 됨을 검증하기 위한 데이터.
UPDATE listings SET deleted_at = now() - interval '1 hour' WHERE id = 4;

-- SERIAL 시퀀스가 수동으로 넣은 id(1~4)와 충돌하지 않도록 다음 값부터 시작하게 보정.
SELECT setval(pg_get_serial_sequence('listings', 'id'), (SELECT MAX(id) FROM listings));
