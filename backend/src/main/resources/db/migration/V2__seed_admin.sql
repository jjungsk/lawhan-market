-- 초기 admin 계정 시딩 (docs/architecture-requirements.md §8)
--
-- 비밀번호는 평문이 아니라 BCrypt 해시로 미리 생성해 넣었다 (Spring Security
-- PasswordEncoder로 로컬에서 생성, 이 SQL에는 해시값만 포함).
-- ON CONFLICT로 재실행해도 중복 삽입되지 않는다.
--
-- *** 이 비밀번호는 임시용이다. 배포 직후 반드시 admin 비밀번호를 변경할 것. ***
INSERT INTO users (email, password_hash, role)
VALUES (
    'admin@lawhan.kr',
    '$2a$10$X1vty4YsGlSGZWjZySs9K.g42FDQzuoylyA13JAU30txe74rWAR42',
    'admin'
)
ON CONFLICT (email) DO NOTHING;
