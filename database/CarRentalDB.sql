-- ============================================================
--  Database: carrentaldb
--  Hệ thống quản lý cho thuê xe máy
--  Phiên bản sạch -- không có comment điều kiện MySQL
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

drop database carrentaldb;
create database carrentaldb;
use carrentaldb;
-- ------------------------------------------------------------
-- ROLES
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `role_id`     INT          NOT NULL AUTO_INCREMENT,
  `role_name`   VARCHAR(50)  NOT NULL,
  `description` TEXT,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uq_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `roles` (`role_id`, `role_name`, `description`) VALUES
(1, 'Admin', 'Quản trị viên toàn quyền hệ thống'),
(2, 'Staff', 'Nhân viên quản lý hợp đồng và khách hàng');

-- ------------------------------------------------------------
-- USERS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id_user`    INT          NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(50)  NOT NULL,
  `password`   VARCHAR(255) NOT NULL,
  `full_name`  VARCHAR(100) NOT NULL,
  `phone`      VARCHAR(15)  DEFAULT NULL,
  `cccd`       VARCHAR(20)  NOT NULL,
  `gender`     TINYINT(1)   DEFAULT 1,
  `birth_date` DATE         DEFAULT NULL,
  `email`      VARCHAR(100) DEFAULT NULL,
  `is_active`  TINYINT(1)   DEFAULT 1,
  `address`    TEXT,
  `role_id`    INT          DEFAULT NULL,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `uq_username` (`username`),
  UNIQUE KEY `uq_cccd_user` (`cccd`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `users` (`id_user`, `username`, `password`, `full_name`, `phone`, `cccd`, `gender`, `birth_date`, `email`, `is_active`, `address`, `role_id`) VALUES
(1, 'admin_quan',    'hashed_pw_001', 'Ngô Lê Anh Quân',        '0905123456', '048099000123', 1, '1990-05-15', 'quan.admin@gmail.com',   1, 'Hải Châu, Đà Nẵng',   1),
(2, 'staff_lan',     'hashed_pw_002', 'Nguyễn Thị Lan',          '0905666777', '048099000456', 0, '1995-10-20', 'lan.staff@gmail.com',    1, 'Liên Chiểu, Đà Nẵng', 2),
(3, 'staff_minh',    'hashed_pw_003', 'Trần Văn Minh',           '0909111222', '048099000789', 1, '1998-03-12', 'minh.staff@gmail.com',   1, 'Ngũ Hành Sơn, Đà Nẵng', 2),
(4, 'staff_huong',   'hashed_pw_004', 'Lê Thị Hương',            '0911333444', '048099001011', 0, '2000-07-25', 'huong.staff@gmail.com',  1, 'Thanh Khê, Đà Nẵng',  2),
(5, 'staff_tuan',    'hashed_pw_005', 'Phạm Anh Tuấn',           '0787709173', '046206008838', 1, '2005-09-14', 'tuan.staff@gmail.com',   1, 'Huế',                 2);

-- ------------------------------------------------------------
-- CUSTOMERS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
  `id_customer`     INT          NOT NULL AUTO_INCREMENT,
  `cccd`            VARCHAR(20)  NOT NULL,
  `full_name`       VARCHAR(100) NOT NULL,
  `gender`          TINYINT(1)   DEFAULT 1,
  `phone`           VARCHAR(15)  DEFAULT NULL,
  `email`           VARCHAR(100) DEFAULT NULL,
  `address`         TEXT,
  `is_blacklist`    TINYINT(1)   DEFAULT 0,
  `blacklist_reason` TEXT,
  `cccd_images`     VARCHAR(255) DEFAULT NULL,
  `trust_score`     INT          DEFAULT 100,
  `rental_count`    INT          DEFAULT 0,
  PRIMARY KEY (`id_customer`),
  UNIQUE KEY `uq_cccd_customer` (`cccd`),
  UNIQUE KEY `uq_email_customer` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `customers` (`id_customer`, `cccd`, `full_name`, `gender`, `phone`, `email`, `address`, `is_blacklist`, `blacklist_reason`, `cccd_images`, `trust_score`, `rental_count`) VALUES
(1,  '079200012301', 'Nguyễn Văn An',       1, '0912000111', 'an.nguyen@gmail.com',      'Hòa Vang, Đà Nẵng',         0, NULL, NULL, 100, 3),
(2,  '079200012302', 'Lê Thị Bình',         0, '0912000222', 'binh.le@gmail.com',         'Sơn Trà, Đà Nẵng',          0, NULL, NULL,  95, 2),
(3,  '079200012303', 'Phạm Văn Cường',      1, '0912000333', 'cuong.pham@gmail.com',      'Quảng Nam',                 0, NULL, NULL, 100, 1),
(4,  '079200012304', 'Trần Thị Dung',       0, '0912000444', 'dung.tran@gmail.com',       'Cam Lệ, Đà Nẵng',           0, NULL, NULL, 100, 2),
(5,  '079200012305', 'Hoàng Văn Em',        1, '0912000555', 'em.hoang@gmail.com',        'Liên Chiểu, Đà Nẵng',       0, NULL, NULL, 100, 1),
(6,  '079200012306', 'Võ Thị Phương',       0, '0912000666', 'phuong.vo@gmail.com',       'Hải Châu, Đà Nẵng',         0, NULL, NULL, 100, 4),
(7,  '079200012307', 'Đặng Văn Giang',      1, '0912000777', 'giang.dang@gmail.com',      'Huế',                       0, NULL, NULL, 100, 2),
(8,  '079200012308', 'Bùi Thị Hoa',         0, '0912000888', 'hoa.bui@gmail.com',         'Quảng Ngãi',                0, NULL, NULL, 100, 1),
(9,  '079200012309', 'Đinh Văn Ích',        1, '0912000999', 'ich.dinh@gmail.com',        'Đà Nẵng',                   0, NULL, NULL,  90, 3),
(10, '079200012310', 'Ngô Thị Kim',         0, '0913001000', 'kim.ngo@gmail.com',          'Thanh Khê, Đà Nẵng',        0, NULL, NULL, 100, 2),
(11, '079200012311', 'Lý Văn Long',         1, '0913001001', 'long.ly@gmail.com',          'Hội An, Quảng Nam',         0, NULL, NULL, 100, 1),
(12, '079200012312', 'Phan Thị Mai',        0, '0913001002', 'mai.phan@gmail.com',         'Ngũ Hành Sơn, Đà Nẵng',    0, NULL, NULL, 100, 2),
(13, '079200012313', 'Vũ Văn Nam',          1, '0913001003', 'nam.vu@gmail.com',            'Đà Nẵng',                   0, NULL, NULL, 100, 1),
(14, '079200012314', 'Cao Thị Oanh',        0, '0913001004', 'oanh.cao@gmail.com',          'Hải Châu, Đà Nẵng',         0, NULL, NULL, 100, 3),
(15, '079200012315', 'Tô Văn Phúc',         1, '0913001005', 'phuc.to@gmail.com',           'Liên Chiểu, Đà Nẵng',       0, NULL, NULL, 100, 1),
(16, '079200012316', 'Dương Thị Quỳnh',     0, '0913001006', 'quynh.duong@gmail.com',      'Huế',                       0, NULL, NULL, 100, 2),
(17, '079200012317', 'Hồ Văn Rạng',         1, '0913001007', 'rang.ho@gmail.com',           'Quảng Nam',                 0, NULL, NULL,  80, 2),
(18, '079200012318', 'Lâm Thị Sen',         0, '0913001008', 'sen.lam@gmail.com',           'Đà Nẵng',                   0, NULL, NULL, 100, 1),
(19, '079200012319', 'Mạc Văn Toàn',        1, '0913001009', 'toan.mac@gmail.com',          'Cam Lệ, Đà Nẵng',           0, NULL, NULL, 100, 2),
(20, '079200012320', 'Nghiêm Thị Uyên',     0, '0913001010', 'uyen.nghiem@gmail.com',      'Sơn Trà, Đà Nẵng',          1, 'Từ chối thanh toán', NULL, 20, 1);

-- ------------------------------------------------------------
-- CUSTOMERDOCUMENTS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `customerdocuments`;
CREATE TABLE `customerdocuments` (
  `id_document`     INT         NOT NULL AUTO_INCREMENT,
  `id_customer`     INT         NOT NULL,
  `document_type`   ENUM('CCCD','BANG LAI','HO CHIEU') NOT NULL,
  `document_number` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`id_document`),
  CONSTRAINT `fk_doc_customer` FOREIGN KEY (`id_customer`) REFERENCES `customers` (`id_customer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `customerdocuments` (`id_document`, `id_customer`, `document_type`, `document_number`) VALUES
(1,  1,  'CCCD',     '079200012301'),
(2,  1,  'BANG LAI', 'BL-100001'),
(3,  2,  'CCCD',     '079200012302'),
(4,  2,  'BANG LAI', 'BL-100002'),
(5,  3,  'CCCD',     '079200012303'),
(6,  4,  'HO CHIEU', 'HC-200004'),
(7,  5,  'CCCD',     '079200012305'),
(8,  6,  'BANG LAI', 'BL-100006'),
(9,  7,  'CCCD',     '079200012307'),
(10, 8,  'BANG LAI', 'BL-100008'),
(11, 9,  'CCCD',     '079200012309'),
(12, 10, 'BANG LAI', 'BL-100010'),
(13, 11, 'HO CHIEU', 'HC-200011'),
(14, 12, 'CCCD',     '079200012312'),
(15, 14, 'BANG LAI', 'BL-100014');

-- ------------------------------------------------------------
-- VEHICLES
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE `vehicles` (
  `id_vehicle`          INT          NOT NULL AUTO_INCREMENT,
  `code_vehicle`        VARCHAR(20)  NOT NULL,
  `brand`               VARCHAR(50)  NOT NULL,
  `model`               VARCHAR(50)  NOT NULL,
  `vehicle_type`        VARCHAR(50)  DEFAULT NULL,
  `color`               VARCHAR(20)  DEFAULT NULL,
  `year_of_manufacture` INT          DEFAULT NULL,
  `price_day`           DOUBLE       DEFAULT NULL,
  `price_hour`          DOUBLE       DEFAULT NULL,
  `fuel_capacity`       INT          DEFAULT NULL,
  `current_km`          INT          DEFAULT 0,
  `maintenance_km`      INT          DEFAULT 5000,
  `image_url`           VARCHAR(255) DEFAULT '/image/card-moto.png',
  `status`              ENUM('AVAILABLE','RENTED','RESERVED','MAINTENANCE','INACTIVE') DEFAULT 'AVAILABLE',
  `purchase_price`      DOUBLE       DEFAULT 0,
  PRIMARY KEY (`id_vehicle`),
  UNIQUE KEY `uq_code_vehicle` (`code_vehicle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `vehicles` (`id_vehicle`, `code_vehicle`, `brand`, `model`, `vehicle_type`, `color`, `year_of_manufacture`, `price_day`, `price_hour`, `fuel_capacity`, `current_km`, `maintenance_km`, `image_url`, `status`, `purchase_price`) VALUES
(1,  '43A-111.11', 'Honda',  'SH 150i',       'Tay ga', 'Đen nhám',      2024, 250000, 35000, 7, 12500, 15000, '/image/card-moto.png', 'AVAILABLE',  120000000),
(2,  '43A-222.22', 'Honda',  'Vision',         'Tay ga', 'Trắng',         2023, 150000, 20000, 5,  8200, 10000, '/image/card-moto.png', 'AVAILABLE',   35000000),
(3,  '43A-333.33', 'Honda',  'Vision',         'Tay ga', 'Đỏ bordeaux',   2022, 140000, 20000, 5,  9100, 12000, '/image/card-moto.png', 'AVAILABLE',   33000000),
(4,  '43B-444.44', 'Honda',  'Air Blade 160',  'Tay ga', 'Xanh xám',      2024, 180000, 25000, 4,  3200,  6000, '/image/card-moto.png', 'RENTED',      60000000),
(5,  '43C-555.55', 'Honda',  'Wave Alpha',     'Xe số',  'Xanh lục',      2023, 100000, 12000, 4,  4700,  7000, '/image/card-moto.png', 'AVAILABLE',   20000000),
(6,  '43D-666.66', 'Yamaha', 'Exciter 155',    'Xe côn', 'Xanh GP',       2024, 180000, 25000, 5,  6500, 10000, '/image/card-moto.png', 'RENTED',      55000000),
(7,  '43E-777.77', 'Yamaha', 'Janus',          'Tay ga', 'Đen',           2022, 130000, 18000, 4,  5300,  8000, '/image/card-moto.png', 'AVAILABLE',   28000000),
(8,  '43F-888.88', 'Honda',  'Winner X',       'Xe côn', 'Camo',          2023, 170000, 22000, 4,  7200,  9000, '/image/card-moto.png', 'AVAILABLE',   45000000),
(9,  '43G-999.99', 'Honda',  'SH Mode',        'Tay ga', 'Xanh',          2024, 200000, 30000, 6,  2100,  5000, '/image/card-moto.png', 'MAINTENANCE', 75000000),
(10, '75H-101010', 'Yamaha', 'Sirius FI',      'Xe số',  'Đỏ',            2021, 120000, 15000, 4,  15000, 18000, '/image/card-moto.png', 'AVAILABLE',  18000000),
(11, '75H-112233', 'Honda',  'Lead 125',       'Tay ga', 'Xám bạc',       2023, 160000, 22000, 5,  4400,  7000, '/image/card-moto.png', 'AVAILABLE',   42000000),
(12, '75H-445566', 'Suzuki', 'Raider R150',    'Xe côn', 'Đỏ đen',        2022, 170000, 23000, 4,  8800, 11000, '/image/card-moto.png', 'AVAILABLE',   50000000),
(13, '43K-778899', 'Honda',  'PCX 125',        'Tay ga', 'Trắng ngọc',    2024, 220000, 32000, 6,  1900,  5000, '/image/card-moto.png', 'AVAILABLE',   95000000),
(14, '43K-001122', 'Yamaha', 'NVX 155',        'Tay ga', 'Đen mờ',        2023, 190000, 28000, 5,  5600,  8000, '/image/card-moto.png', 'RENTED',      68000000),
(15, '43L-334455', 'Honda',  'Future 125 FI',  'Xe số',  'Xanh dương',    2022, 110000, 14000, 4,  11000, 14000, '/image/card-moto.png', 'AVAILABLE',  22000000);

-- ------------------------------------------------------------
-- VOUCHERS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `vouchers`;
CREATE TABLE `vouchers` (
  `id_voucher`     INT         NOT NULL AUTO_INCREMENT,
  `code_voucher`   VARCHAR(20) NOT NULL,
  `description`    TEXT,
  `discount_type`  ENUM('CO DINH','PHAN TRAM') NOT NULL,
  `discount_value` DOUBLE      DEFAULT NULL,
  `usage_limit`    INT         DEFAULT 0,
  `usage_count`    INT         DEFAULT 0,
  `valid_from_date` DATE       NOT NULL,
  `valid_to_date`  DATE        NOT NULL,
  `is_active`      TINYINT(1)  DEFAULT 1,
  PRIMARY KEY (`id_voucher`),
  UNIQUE KEY `uq_code_voucher` (`code_voucher`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `vouchers` (`id_voucher`, `code_voucher`, `description`, `discount_type`, `discount_value`, `usage_limit`, `usage_count`, `valid_from_date`, `valid_to_date`, `is_active`) VALUES
(1, 'WELCOME50K',   'Giảm 50.000đ cho khách mới',          'CO DINH',  50000, 200, 12, '2026-01-01', '2026-12-31', 1),
(2, 'TET2026',      'Giảm 10% dịp Tết Nguyên Đán',         'PHAN TRAM', 10,    50,  8, '2026-01-20', '2026-02-05', 0),
(3, 'SUMMER30K',    'Giảm 30.000đ mùa hè 2026',            'CO DINH',  30000, 100,  5, '2026-05-01', '2026-08-31', 1),
(4, 'LOYAL15',      'Khách VIP - giảm 15%',                 'PHAN TRAM', 15,    30,  3, '2026-01-01', '2026-12-31', 1),
(5, 'WEEKEND20K',   'Thuê cuối tuần giảm 20.000đ',          'CO DINH',  20000, 500, 47, '2026-01-01', '2026-12-31', 1),
(6, 'BIRTHDAY100K', 'Mừng sinh nhật - giảm 100.000đ',       'CO DINH', 100000,  50,  2, '2026-06-01', '2026-06-30', 1);

-- ------------------------------------------------------------
-- CONTRACTS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `contracts`;
CREATE TABLE `contracts` (
  `id_contract`    INT          NOT NULL AUTO_INCREMENT,
  `code_contract`  VARCHAR(20)  NOT NULL,
  `start_datetime` DATETIME     NOT NULL,
  `end_datetime`   DATETIME     NOT NULL,
  `return_datetime` DATETIME    DEFAULT NULL,
  `km_start`       INT          NOT NULL DEFAULT 0,
  `km_end`         INT          DEFAULT NULL,
  `fuel_start`     INT          DEFAULT 100,
  `fuel_end`       INT          DEFAULT NULL,
  `deposit_type`   ENUM('TIEN MAT','GIAY TO','KHAC') DEFAULT 'TIEN MAT',
  `deposit_amount` DOUBLE       DEFAULT 0,
  `base_price`     DOUBLE       DEFAULT NULL,
  `discount_amount` DOUBLE      DEFAULT 0,
  `total_price`    DOUBLE       DEFAULT NULL,
  `payment_status` ENUM('CHUA THANH TOAN','THANH TOAN 1 PHAN','DA THANH TOAN') DEFAULT 'CHUA THANH TOAN',
  `status`         ENUM('DANG THUE','QUA HAN','HOAN THANH','DA HUY') DEFAULT NULL,
  `id_user`        INT          NOT NULL,
  `id_vehicle`     INT          NOT NULL,
  `id_customer`    INT          NOT NULL,
  `id_voucher`     INT          DEFAULT NULL,
  PRIMARY KEY (`id_contract`),
  UNIQUE KEY `uq_code_contract` (`code_contract`),
  CONSTRAINT `fk_contract_user`     FOREIGN KEY (`id_user`)     REFERENCES `users`     (`id_user`),
  CONSTRAINT `fk_contract_vehicle`  FOREIGN KEY (`id_vehicle`)  REFERENCES `vehicles`  (`id_vehicle`),
  CONSTRAINT `fk_contract_customer` FOREIGN KEY (`id_customer`) REFERENCES `customers` (`id_customer`),
  CONSTRAINT `fk_contract_voucher`  FOREIGN KEY (`id_voucher`)  REFERENCES `vouchers`  (`id_voucher`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `contracts` (`id_contract`, `code_contract`, `start_datetime`, `end_datetime`, `return_datetime`, `km_start`, `km_end`, `fuel_start`, `fuel_end`, `deposit_type`, `deposit_amount`, `base_price`, `discount_amount`, `total_price`, `payment_status`, `status`, `id_user`, `id_vehicle`, `id_customer`, `id_voucher`) VALUES
(1,  'HD-20260101-001', '2026-01-05 08:00:00', '2026-01-06 08:00:00', '2026-01-06 09:00:00', 8000,  8155,  100, 90,  'TIEN MAT', 500000, 250000,  50000,  200000, 'DA THANH TOAN', 'HOAN THANH', 2, 1,  1,  1),
(2,  'HD-20260108-002', '2026-01-08 09:00:00', '2026-01-10 09:00:00', '2026-01-10 10:00:00', 3500,  3780,  100, 85,  'GIAY TO',       0, 300000,      0,  300000, 'DA THANH TOAN', 'HOAN THANH', 2, 13,  2, NULL),
(3,  'HD-20260115-003', '2026-01-15 08:00:00', '2026-01-16 08:00:00', '2026-01-16 08:30:00', 4200,  4350,  100, 100, 'TIEN MAT', 300000, 180000,  20000,  160000, 'DA THANH TOAN', 'HOAN THANH', 1, 6,  3,  5),
(4,  'HD-20260120-004', '2026-01-20 07:00:00', '2026-01-22 07:00:00', '2026-01-22 08:00:00', 9100,  9480,  100, 80,  'GIAY TO',       0, 360000,  36000,  324000, 'DA THANH TOAN', 'HOAN THANH', 3, 14,  4,  2),
(5,  'HD-20260201-005', '2026-02-01 08:00:00', '2026-02-03 08:00:00', '2026-02-03 07:45:00', 5000,  5200,  100, 100, 'TIEN MAT', 400000, 280000,      0,  280000, 'DA THANH TOAN', 'HOAN THANH', 2, 7,  5, NULL),
(6,  'HD-20260210-006', '2026-02-10 09:00:00', '2026-02-11 09:00:00', '2026-02-11 10:30:00', 7800,  7950,  100, 95,  'TIEN MAT', 200000, 150000,  30000,  120000, 'DA THANH TOAN', 'HOAN THANH', 1, 5,  6,  5),
(7,  'HD-20260220-007', '2026-02-20 08:00:00', '2026-02-22 08:00:00', '2026-02-23 11:00:00', 12500, 12800,  100, 90,  'GIAY TO',       0, 340000,      0,  430000, 'DA THANH TOAN', 'HOAN THANH', 2, 8,  7, NULL),
(8,  'HD-20260305-008', '2026-03-05 08:00:00', '2026-03-06 08:00:00', '2026-03-06 08:00:00', 4400,  4550,  100, 100, 'TIEN MAT', 500000, 220000,  50000,  170000, 'DA THANH TOAN', 'HOAN THANH', 3, 11,  8,  1),
(9,  'HD-20260312-009', '2026-03-12 09:00:00', '2026-03-14 09:00:00', '2026-03-14 09:30:00', 15000, 15320,  100, 85,  'GIAY TO',       0, 360000,      0,  360000, 'DA THANH TOAN', 'HOAN THANH', 1, 10,  9, NULL),
(10, 'HD-20260325-010', '2026-03-25 07:30:00', '2026-03-27 07:30:00', '2026-03-27 08:00:00', 8800,  9050,  100, 100, 'TIEN MAT', 300000, 300000,  15000,  285000, 'DA THANH TOAN', 'HOAN THANH', 4, 2,  10, 5),
(11, 'HD-20260402-011', '2026-04-02 08:00:00', '2026-04-03 08:00:00', '2026-04-03 09:00:00', 9100,  9250,  100, 95,  'TIEN MAT', 200000, 190000,  50000,  140000, 'DA THANH TOAN', 'HOAN THANH', 2, 3,  11, 1),
(12, 'HD-20260410-012', '2026-04-10 08:00:00', '2026-04-12 08:00:00', '2026-04-12 10:00:00', 5600,  5820,  100, 80,  'GIAY TO',       0, 400000,  60000,  360000, 'DA THANH TOAN', 'HOAN THANH', 3, 12,  12, 4),
(13, 'HD-20260420-013', '2026-04-20 09:00:00', '2026-04-22 09:00:00', '2026-04-22 09:00:00', 11000, 11280,  100, 100, 'TIEN MAT', 400000, 240000,      0,  240000, 'DA THANH TOAN', 'HOAN THANH', 1, 5,  13, NULL),
(14, 'HD-20260501-014', '2026-05-01 08:00:00', '2026-05-03 08:00:00', '2026-05-03 09:30:00', 3200,  3480,  100, 90,  'TIEN MAT', 500000, 360000,  20000,  340000, 'DA THANH TOAN', 'HOAN THANH', 5, 6,  14, 5),
(15, 'HD-20260510-015', '2026-05-10 08:00:00', '2026-05-11 08:00:00', '2026-05-11 08:00:00', 5000,  5130,  100, 100, 'GIAY TO',       0, 180000,      0,  180000, 'DA THANH TOAN', 'HOAN THANH', 2, 15,  15, NULL),
(16, 'HD-20260515-016', '2026-05-15 08:00:00', '2026-05-17 08:00:00', '2026-05-18 10:00:00', 7200,  7500,  100, 80,  'TIEN MAT', 300000, 280000,  30000,  290000, 'DA THANH TOAN', 'HOAN THANH', 1, 7,  16, 5),
(17, 'HD-20260520-017', '2026-05-20 09:00:00', '2026-05-22 09:00:00', '2026-05-22 09:00:00', 8200,  8420,  100, 100, 'GIAY TO',       0, 340000,  51000,  289000, 'DA THANH TOAN', 'HOAN THANH', 3, 14,  17, 4),
(18, 'HD-20260525-018', '2026-05-25 08:00:00', '2026-05-26 08:00:00', '2026-05-26 09:00:00', 4400,  4560,  100, 90,  'TIEN MAT', 200000, 160000,  50000,  110000, 'DA THANH TOAN', 'HOAN THANH', 4, 11,  18, 1),
(19, 'HD-20260601-019', '2026-06-01 08:00:00', '2026-06-03 08:00:00', NULL,                   6500,  NULL,  100, NULL,'TIEN MAT', 600000, 380000,  50000,  330000, 'THANH TOAN 1 PHAN', 'DANG THUE', 5, 4,  19, 1),
(20, 'HD-20260603-020', '2026-06-03 09:00:00', '2026-06-05 09:00:00', NULL,                   3200,  NULL,  100, NULL,'GIAY TO',       0, 360000,      0,  360000, 'CHUA THANH TOAN', 'DANG THUE', 1, 6,  20, NULL);

-- ------------------------------------------------------------
-- INSPECTIONS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `inspections`;
CREATE TABLE `inspections` (
  `id_inspection`   INT NOT NULL AUTO_INCREMENT,
  `id_contract`     INT NOT NULL,
  `id_user`         INT NOT NULL,
  `inspection_type` ENUM('GIAO XE','TRA XE') NOT NULL,
  PRIMARY KEY (`id_inspection`),
  CONSTRAINT `fk_insp_contract` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`),
  CONSTRAINT `fk_insp_user`     FOREIGN KEY (`id_user`)     REFERENCES `users`     (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `inspections` (`id_inspection`, `id_contract`, `id_user`, `inspection_type`) VALUES
(1,  1,  2, 'GIAO XE'), (2,  1,  2, 'TRA XE'),
(3,  2,  2, 'GIAO XE'), (4,  2,  3, 'TRA XE'),
(5,  3,  1, 'GIAO XE'), (6,  3,  1, 'TRA XE'),
(7,  4,  3, 'GIAO XE'), (8,  4,  3, 'TRA XE'),
(9,  5,  2, 'GIAO XE'), (10, 5,  2, 'TRA XE'),
(11, 6,  1, 'GIAO XE'), (12, 6,  4, 'TRA XE'),
(13, 7,  2, 'GIAO XE'), (14, 7,  1, 'TRA XE'),
(15, 8,  3, 'GIAO XE'), (16, 8,  3, 'TRA XE'),
(17, 9,  1, 'GIAO XE'), (18, 9,  1, 'TRA XE'),
(19, 10, 4, 'GIAO XE'), (20, 10, 4, 'TRA XE'),
(21, 19, 5, 'GIAO XE'),
(22, 20, 1, 'GIAO XE');

-- ------------------------------------------------------------
-- PAYMENTS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
  `id_payment`     INT    NOT NULL AUTO_INCREMENT,
  `amount`         DOUBLE NOT NULL,
  `payment_type`   ENUM('TIEN COC','THANH TOAN PHAN CON LAI','HOAN TIEN','PHU THU') NOT NULL,
  `payment_method` ENUM('TIEN MAT','CHUYEN KHOAN') DEFAULT 'TIEN MAT',
  `id_user`        INT    NOT NULL,
  `id_contract`    INT    NOT NULL,
  PRIMARY KEY (`id_payment`),
  CONSTRAINT `fk_pay_user`     FOREIGN KEY (`id_user`)     REFERENCES `users`     (`id_user`),
  CONSTRAINT `fk_pay_contract` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `payments` (`id_payment`, `amount`, `payment_type`, `payment_method`, `id_user`, `id_contract`) VALUES
(1,  500000, 'TIEN COC',                'TIEN MAT',    2,  1),
(2,  200000, 'THANH TOAN PHAN CON LAI', 'CHUYEN KHOAN',2,  1),
(3,  300000, 'THANH TOAN PHAN CON LAI', 'CHUYEN KHOAN',2,  2),
(4,  160000, 'THANH TOAN PHAN CON LAI', 'TIEN MAT',    1,  3),
(5,  324000, 'THANH TOAN PHAN CON LAI', 'CHUYEN KHOAN',3,  4),
(6,  280000, 'THANH TOAN PHAN CON LAI', 'TIEN MAT',    2,  5),
(7,  200000, 'TIEN COC',                'TIEN MAT',    1,  6),
(8,  120000, 'THANH TOAN PHAN CON LAI', 'TIEN MAT',    1,  6),
(9,  430000, 'THANH TOAN PHAN CON LAI', 'CHUYEN KHOAN',2,  7),
(10, 500000, 'TIEN COC',                'TIEN MAT',    3,  8),
(11, 170000, 'THANH TOAN PHAN CON LAI', 'TIEN MAT',    3,  8),
(12, 360000, 'THANH TOAN PHAN CON LAI', 'CHUYEN KHOAN',1,  9),
(13, 285000, 'THANH TOAN PHAN CON LAI', 'TIEN MAT',    4,  10),
(14, 340000, 'THANH TOAN PHAN CON LAI', 'CHUYEN KHOAN',5,  14),
(15, 600000, 'TIEN COC',                'TIEN MAT',    5,  19),
(16, 100000, 'THANH TOAN PHAN CON LAI', 'CHUYEN KHOAN',5,  19);

-- ------------------------------------------------------------
-- PENALTIES
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `penalties`;
CREATE TABLE `penalties` (
  `id_penalty`   INT    NOT NULL AUTO_INCREMENT,
  `id_contract`  INT    NOT NULL,
  `penalty_type` ENUM('QUA GIO','XANG','HU HONG','VI PHAM GIAO THONG') DEFAULT NULL,
  `amount`       DOUBLE DEFAULT NULL,
  PRIMARY KEY (`id_penalty`),
  CONSTRAINT `fk_pen_contract` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `penalties` (`id_penalty`, `id_contract`, `penalty_type`, `amount`) VALUES
(1,  7,  'QUA GIO',             90000),
(2,  7,  'HU HONG',            150000),
(3,  12, 'QUA GIO',             20000),
(4,  16, 'QUA GIO',            100000),
(5,  16, 'XANG',                50000),
(6,  2,  'VI PHAM GIAO THONG', 500000);

-- ------------------------------------------------------------
-- PARTPRICES
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `partprices`;
CREATE TABLE `partprices` (
  `id_part_price` INT          NOT NULL AUTO_INCREMENT,
  `part_name`     VARCHAR(100) NOT NULL,
  `vehicle_type`  VARCHAR(50)  DEFAULT NULL,
  `price`         DOUBLE       DEFAULT NULL,
  PRIMARY KEY (`id_part_price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `partprices` (`id_part_price`, `part_name`, `vehicle_type`, `price`) VALUES
(1, 'Đèn pha',      'Honda',    350000),
(2, 'Đèn pha',      'Yamaha',   320000),
(3, 'Vỏ xe sau',    'Tất cả',   250000),
(4, 'Vỏ xe trước',  'Tất cả',   220000),
(5, 'Tay ga',       'Tất cả',    25000),
(6, 'Bugi',         'Tất cả',    50000),
(7, 'Gương chiếu',  'Tất cả',    80000),
(8, 'Phanh đĩa',    'Tất cả',   200000),
(9, 'Yếm xe',       'Tay ga',   450000),
(10,'Nhông xích',   'Xe số',    180000);

-- ------------------------------------------------------------
-- RULES
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `rules`;
CREATE TABLE `rules` (
  `id_rule`    INT         NOT NULL AUTO_INCREMENT,
  `rule_name`  VARCHAR(100) NOT NULL,
  `rule_type`  ENUM('CUOI TUAN','NGAY LE','KHAC') NOT NULL,
  `multi`      DOUBLE      NOT NULL,
  `start_date` DATE        NOT NULL,
  `end_date`   DATE        NOT NULL,
  `is_active`  TINYINT(1)  DEFAULT 0,
  PRIMARY KEY (`id_rule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `rules` (`id_rule`, `rule_name`, `rule_type`, `multi`, `start_date`, `end_date`, `is_active`) VALUES
(1, 'Giá cuối tuần',         'CUOI TUAN', 1.2, '2026-01-01', '2026-12-31', 1),
(2, 'Giá Tết Nguyên Đán',   'NGAY LE',   1.5, '2026-01-25', '2026-02-02', 0),
(3, 'Lễ 30/4 - 1/5',        'NGAY LE',   1.3, '2026-04-30', '2026-05-01', 0),
(4, 'Lễ Quốc Khánh 2/9',    'NGAY LE',   1.3, '2026-09-02', '2026-09-03', 1),
(5, 'Mùa hè (6-8)',          'KHAC',      1.1, '2026-06-01', '2026-08-31', 1);

-- ------------------------------------------------------------
-- SYSTEMSETTINGS
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `systemsettings`;
CREATE TABLE `systemsettings` (
  `id_setting`    INT          NOT NULL AUTO_INCREMENT,
  `setting_key`   VARCHAR(100) NOT NULL,
  `setting_value` TEXT         NOT NULL,
  `data_type`     ENUM('STRING','NUMBER','BOOLEAN','JSON') DEFAULT 'STRING',
  `description`   TEXT,
  `category`      VARCHAR(50)  DEFAULT NULL,
  `user_id`       INT          NOT NULL,
  `update_at`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_setting`),
  UNIQUE KEY `uq_setting_key` (`setting_key`),
  CONSTRAINT `fk_setting_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `systemsettings` (`id_setting`, `setting_key`, `setting_value`, `data_type`, `description`, `category`, `user_id`) VALUES
(1, 'Gia_Xang_Litre', '23000', 'NUMBER', 'Giá xăng thị trường dùng để tính phạt', 'pricing', 1),
(2, 'Phi_Tre_Gio',    '10000', 'NUMBER', 'Phí phạt trễ mỗi giờ mặc định',         'penalty', 1),
(3, 'Phi_Vuot_Km',    '5000',  'NUMBER', 'Phí vượt km giới hạn mỗi km',           'penalty', 1),
(4, 'Ten_Cua_Hang',   'Thuê Xe Đà Nẵng', 'STRING', 'Tên hiển thị của cửa hàng',   'general', 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Hoàn tất -- carrentaldb_clean.sql
-- ============================================================