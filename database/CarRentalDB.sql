-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: carrentaldb
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `contracts`
--

DROP TABLE IF EXISTS `contracts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contracts` (
  `id_contract` int NOT NULL AUTO_INCREMENT,
  `code_contract` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_datetime` datetime NOT NULL,
  `end_datetime` datetime NOT NULL,
  `return_datetime` datetime DEFAULT NULL,
  `km_start` int NOT NULL DEFAULT '0',
  `km_end` int DEFAULT NULL,
  `fuel_start` int DEFAULT '100',
  `fuel_end` int DEFAULT NULL,
  `deposit_type` enum('TIEN MAT','GIAY TO','KHAC') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'TIEN MAT',
  `deposit_amount` double DEFAULT '0',
  `base_price` double DEFAULT NULL,
  `discount_amount` double DEFAULT '0',
  `total_price` double DEFAULT NULL,
  `payment_status` enum('CHUA THANH TOAN','THANH TOAN 1 PHAN','DA THANH TOAN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'CHUA THANH TOAN',
  `status` enum('DANG THUE','QUA HAN','HOAN THANH','DA HUY') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_user` int NOT NULL,
  `id_vehicle` int NOT NULL,
  `id_customer` int NOT NULL,
  `id_voucher` int DEFAULT NULL,
  PRIMARY KEY (`id_contract`),
  UNIQUE KEY `uq_code_contract` (`code_contract`),
  KEY `fk_contract_user` (`id_user`),
  KEY `fk_contract_vehicle` (`id_vehicle`),
  KEY `fk_contract_customer` (`id_customer`),
  KEY `fk_contract_voucher` (`id_voucher`),
  CONSTRAINT `fk_contract_customer` FOREIGN KEY (`id_customer`) REFERENCES `customers` (`id_customer`),
  CONSTRAINT `fk_contract_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  CONSTRAINT `fk_contract_vehicle` FOREIGN KEY (`id_vehicle`) REFERENCES `vehicles` (`id_vehicle`),
  CONSTRAINT `fk_contract_voucher` FOREIGN KEY (`id_voucher`) REFERENCES `vouchers` (`id_voucher`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contracts`
--

LOCK TABLES `contracts` WRITE;
/*!40000 ALTER TABLE `contracts` DISABLE KEYS */;
INSERT INTO `contracts` VALUES (1,'HD-001','2026-01-10 08:00:00','2026-01-11 08:00:00','2026-01-11 09:00:00',1000,1150,100,80,'TIEN MAT',500000,150000,0,180000,'DA THANH TOAN','HOAN THANH',2,1,1,NULL),(2,'HD-002','2026-01-24 08:00:00','2026-01-26 08:00:00',NULL,3500,NULL,100,NULL,'GIAY TO',0,240000,24000,216000,'CHUA THANH TOAN','DANG THUE',2,2,2,2),(3,'HD-003','2026-04-24 08:00:00','2026-04-25 08:00:00',NULL,1500,NULL,100,NULL,'TIEN MAT',0,100000,0,100000,'CHUA THANH TOAN','DANG THUE',1,5,6,NULL),(4,'HD-004','2026-04-25 08:00:00','2026-04-26 08:00:00',NULL,1200,NULL,100,NULL,'TIEN MAT',0,150000,0,150000,'CHUA THANH TOAN','DANG THUE',1,2,6,NULL);
/*!40000 ALTER TABLE `contracts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customerdocuments`
--

DROP TABLE IF EXISTS `customerdocuments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customerdocuments` (
  `id_document` int NOT NULL AUTO_INCREMENT,
  `id_customer` int NOT NULL,
  `document_type` enum('CCCD','BANG LAI','HO CHIEU') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `document_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_document`),
  KEY `fk_doc_customer` (`id_customer`),
  CONSTRAINT `fk_doc_customer` FOREIGN KEY (`id_customer`) REFERENCES `customers` (`id_customer`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customerdocuments`
--

LOCK TABLES `customerdocuments` WRITE;
/*!40000 ALTER TABLE `customerdocuments` DISABLE KEYS */;
INSERT INTO `customerdocuments` VALUES (1,1,'CCCD','012345678901'),(2,1,'BANG LAI','BL-999888'),(3,2,'HO CHIEU','HC-111222');
/*!40000 ALTER TABLE `customerdocuments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id_customer` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` tinyint(1) DEFAULT '1',
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `is_blacklist` tinyint(1) DEFAULT '0',
  `blacklist_reason` text COLLATE utf8mb4_unicode_ci,
  `cccd_images` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `trust_score` int DEFAULT '100',
  `rental_count` int DEFAULT '0',
  PRIMARY KEY (`id_customer`),
  UNIQUE KEY `uq_cccd_customer` (`cccd`),
  UNIQUE KEY `uq_email_customer` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'012345678901','Nguyễn Văn An',1,'0912000111','an.nguyen@gmail.com','Hòa Vang, Đà Nẵng',0,NULL,'images/cccd/012345678901.jpg',100,2),(2,'012345678902','Lê Thị Bình',0,'0122440959','binh.le@gmail.com','Sơn Trà, Đà Nẵng',0,NULL,'012345678902',95,3),(3,'012345678903','Phạm Văn Xấu',1,'0912444555','xau.pham@gmail.com','Quảng Nam',1,'Quỵt tiền thuê xe năm 2024','images/cccd/012345678903.jpg',10,4),(4,'04652305312','Bin Lê',1,'012244098',NULL,'Da nang',0,NULL,NULL,100,0),(5,'1234','Ngô Văn Việt',1,'0122440959','viet@gmail.com','Huế',0,NULL,NULL,100,0),(6,'046206008838','Ngô Lê Anh Vũ',1,'0121654918','vuvn@gmail.com','Nguyễn Khuyến',0,NULL,NULL,100,0);
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inspections`
--

DROP TABLE IF EXISTS `inspections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inspections` (
  `id_inspection` int NOT NULL AUTO_INCREMENT,
  `id_contract` int NOT NULL,
  `id_user` int NOT NULL,
  `inspection_type` enum('GIAO XE','TRA XE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_inspection`),
  KEY `fk_insp_contract` (`id_contract`),
  KEY `fk_insp_user` (`id_user`),
  CONSTRAINT `fk_insp_contract` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`),
  CONSTRAINT `fk_insp_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inspections`
--

LOCK TABLES `inspections` WRITE;
/*!40000 ALTER TABLE `inspections` DISABLE KEYS */;
/*!40000 ALTER TABLE `inspections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `partprices`
--

DROP TABLE IF EXISTS `partprices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `partprices` (
  `id_part_price` int NOT NULL AUTO_INCREMENT,
  `part_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehicle_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  PRIMARY KEY (`id_part_price`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `partprices`
--

LOCK TABLES `partprices` WRITE;
/*!40000 ALTER TABLE `partprices` DISABLE KEYS */;
INSERT INTO `partprices` VALUES (2,'Gương chiếu hậu','Xe số',100000),(3,'Đèn pha','Honda',350000),(4,'Vỏ xe sau','Tất cả',250000),(5,'Tay ga','Tất cả',25000),(6,'Bugi','Tất cả',150000);
/*!40000 ALTER TABLE `partprices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id_payment` int NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `payment_type` enum('TIEN COC','THANH TOAN PHAN CON LAI','HOAN TIEN','PHU THU') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_method` enum('TIEN MAT','CHUYEN KHOAN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'TIEN MAT',
  `id_user` int NOT NULL,
  `id_contract` int NOT NULL,
  PRIMARY KEY (`id_payment`),
  KEY `fk_pay_user` (`id_user`),
  KEY `fk_pay_contract` (`id_contract`),
  CONSTRAINT `fk_pay_contract` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`),
  CONSTRAINT `fk_pay_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,500000,'TIEN COC','TIEN MAT',2,1),(2,180000,'THANH TOAN PHAN CON LAI','CHUYEN KHOAN',2,1);
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `penalties`
--

DROP TABLE IF EXISTS `penalties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `penalties` (
  `id_penalty` int NOT NULL AUTO_INCREMENT,
  `id_contract` int NOT NULL,
  `penalty_type` enum('QUA GIO','XANG','HU HONG','VI PHAM GIAO THONG') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `amount` double DEFAULT NULL,
  PRIMARY KEY (`id_penalty`),
  KEY `fk_pen_contract` (`id_contract`),
  CONSTRAINT `fk_pen_contract` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `penalties`
--

LOCK TABLES `penalties` WRITE;
/*!40000 ALTER TABLE `penalties` DISABLE KEYS */;
INSERT INTO `penalties` VALUES (1,1,'QUA GIO',20000),(2,1,'XANG',10000);
/*!40000 ALTER TABLE `penalties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uq_role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Admin','Quản trị viên toàn quyền hệ thống'),(2,'Staff','Nhân viên quản lý hợp đồng và khách hàng');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rules`
--

DROP TABLE IF EXISTS `rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rules` (
  `id_rule` int NOT NULL AUTO_INCREMENT,
  `rule_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rule_type` enum('CUOI TUAN','NGAY LE','KHAC') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `multi` double NOT NULL,
  `star_date` date NOT NULL,
  `end_date` date NOT NULL,
  `is_active` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id_rule`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rules`
--

LOCK TABLES `rules` WRITE;
/*!40000 ALTER TABLE `rules` DISABLE KEYS */;
INSERT INTO `rules` VALUES (1,'Giá cuối tuần','CUOI TUAN',1.2,'2026-01-01','2026-12-31',1),(2,'Giá Tết Nguyên Đán','NGAY LE',1.5,'2026-01-25','2026-02-02',1);
/*!40000 ALTER TABLE `rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `systemsettings`
--

DROP TABLE IF EXISTS `systemsettings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `systemsettings` (
  `id_setting` int NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `setting_value` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_type` enum('STRING','NUMBER','BOOLEAN','JSON') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'STRING',
  `description` text COLLATE utf8mb4_unicode_ci,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` int NOT NULL,
  `update_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_setting`),
  UNIQUE KEY `uq_setting_key` (`setting_key`),
  KEY `fk_setting_user` (`user_id`),
  CONSTRAINT `fk_setting_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `systemsettings`
--

LOCK TABLES `systemsettings` WRITE;
/*!40000 ALTER TABLE `systemsettings` DISABLE KEYS */;
INSERT INTO `systemsettings` VALUES (1,'Gia_Xang_Litre','25000','NUMBER','Giá xăng thị trường dùng để tính phạt','pricing',1,'2026-04-24 08:20:05'),(2,'Phi_Tre_Gio','100000','NUMBER','Phí phạt trễ mỗi giờ mặc định','penalty',1,'2026-04-24 08:20:05');
/*!40000 ALTER TABLE `systemsettings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id_user` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` tinyint(1) DEFAULT '1',
  `birth_date` date DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `address` text COLLATE utf8mb4_unicode_ci,
  `role_id` int DEFAULT NULL,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `uq_username` (`username`),
  UNIQUE KEY `uq_cccd_user` (`cccd`),
  KEY `fk_user_role` (`role_id`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin_quan','password123','Ngô Lê Anh Quân','0905123456','048099000123',1,'1990-05-15','quan.admin@gmail.com',1,'Hải Châu, Đà Nẵng',1),(2,'staff_lan','staff789','Nguyễn Thị Lan','0905666777','048099000456',0,'1995-10-20','lan.staff@gmail.com',1,'Liên Chiểu, Đà Nẵng',2),(3,'quan_staff','12345678','Ngô Lê Anh Quân','0905123456','123456789012',1,'2004-10-20','quan@gmail.com',1,'Đà Nẵng',2),(4,'nguyen_staff','12345678','Mai Nguyễn Đạt Nguyên','0905654321','987654321098',1,'2004-05-15','nguyen@gmail.com',1,'Quảng Nam',2);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicles`
--

DROP TABLE IF EXISTS `vehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicles` (
  `id_vehicle` int NOT NULL AUTO_INCREMENT,
  `code_vehicle` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `brand` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehicle_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `year_of_manufacture` int DEFAULT NULL,
  `price_day` double DEFAULT NULL,
  `price_hour` double DEFAULT NULL,
  `fuel_capacity` int DEFAULT NULL,
  `current_km` int DEFAULT '0',
  `maintenance_km` int DEFAULT '5000',
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '/image/dashboardform/card-moto.png',
  `status` enum('AVAILABLE','RENTED','RESERVED','MAINTENANCE','INACTIVE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'AVAILABLE',
  `purchase_price` double DEFAULT '0',
  PRIMARY KEY (`id_vehicle`),
  UNIQUE KEY `uq_code_vehicle` (`code_vehicle`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicles`
--

LOCK TABLES `vehicles` WRITE;
/*!40000 ALTER TABLE `vehicles` DISABLE KEYS */;
INSERT INTO `vehicles` VALUES (1,'43A-111.11','Honda','SH 150i','Tay ga','Đen nhám',2024,250000,35000,7,500,5000,'/image/dashboardform/card-moto.png','AVAILABLE',120000000),(2,'43A-222.22','Honda','Vision','Tay ga','Trắng',2023,150000,20000,5,1200,5000,'/image/dashboardform/card-moto.png','RENTED',35000000),(3,'43A-333.33','Honda','Vision','Tay ga','Đỏ bordeaux',2022,140000,20000,5,8500,8000,'/image/dashboardform/card-moto.png','MAINTENANCE',33000000),(4,'43B-444.44','Honda','Air Blade 160','Tay ga','Xanh xám',2024,180000,25000,4,300,5000,'/image/dashboardform/card-moto.png','RENTED',60000000),(5,'43C-555.55','Honda','Wave Alpha','Xe số','Xanh lục',2023,100000,12000,4,1500,5000,'/image/dashboardform/card-moto.png','RENTED',20000000),(6,'43D-666.66','Yamaha','Exciter 155','Xe côn','Xanh GP',2024,180000,25000,5,2000,5000,'/image/dashboardform/card-moto.png','AVAILABLE',55000000),(7,'43E-777.77','Yamaha','Janus','Tay ga','Vàng cát',2022,130000,18000,4,4200,5000,'/image/dashboardform/card-moto.png','AVAILABLE',28000000),(8,'43F-888.88','Honda','Winner X','Xe côn','Camo',2023,170000,22000,4,6000,7000,'/image/dashboardform/card-moto.png','RENTED',45000000),(9,'43G-999.99','Honda','SH Mode','Tay ga','Xanh ngọc',2024,200000,30000,6,100,5000,'/image/dashboardform/card-moto.png','AVAILABLE',75000000),(10,'75H-989999','Yamaha','Sirius','Xe số','Đỏ',2017,150000,15000,0,1920,10000,'/image/dashboardform/card-moto.png','AVAILABLE',2000000);
/*!40000 ALTER TABLE `vehicles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id_voucher` int NOT NULL AUTO_INCREMENT,
  `code_voucher` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `discount_type` enum('CO DINH','PHAN TRAM') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_value` double DEFAULT NULL,
  `usage_limit` int DEFAULT '0',
  `usage_count` int DEFAULT '0',
  `valid_from_date` date NOT NULL,
  `valid_to_date` date NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id_voucher`),
  UNIQUE KEY `uq_code_voucher` (`code_voucher`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` VALUES (1,'HELLO2026','Giảm 50k cho khách mới','CO DINH',50000,100,5,'2026-01-01','2026-12-31',1),(2,'TET2026','Giảm 10% dịp Tết','PHAN TRAM',10,50,0,'2026-01-20','2026-02-05',0),(3,'SUMMER2026','Giảm 25k mùa hè','CO DINH',25000,25,0,'2026-04-24','2026-05-24',1),(5,'NGHIHE2026','','CO DINH',40000,100,0,'2026-04-25','2026-06-06',1);
/*!40000 ALTER TABLE `vouchers` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-25  8:07:28
