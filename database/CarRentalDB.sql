contractscustomerdocumentscustomers-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
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
  `id_contract` int NOT NULL,
  `code_contract` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_datetime` datetime NOT NULL,
  `end_datetime` datetime NOT NULL,
  `return_datetime` datetime DEFAULT NULL,
  `km_start` int NOT NULL,
  `km_end` int DEFAULT NULL,
  `fuel_start` int DEFAULT '100',
  `fuel_end` int DEFAULT NULL,
  `deposit_type` enum('TIEN MAT','GIAY TO','KHAC') COLLATE utf8mb4_unicode_ci DEFAULT 'TIEN MAT',
  `deposit_amount` double DEFAULT '0',
  `base_price` double DEFAULT NULL,
  `discount_amount` double DEFAULT '0',
  `total_price` double DEFAULT NULL,
  `payment_status` enum('CHUA THANH TOAN','THANH TOAN 1 PHAN','DA THANH TOAN') COLLATE utf8mb4_unicode_ci DEFAULT 'CHUA THANH TOAN',
  `status` enum('DANG THUE','QUA HAN','HOAN THANH','DA HUY') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_user` int NOT NULL,
  `id_vehicle` int NOT NULL,
  `id_customer` int NOT NULL,
  `id_voucher` int DEFAULT NULL,
  PRIMARY KEY (`id_contract`),
  UNIQUE KEY `code_contract` (`code_contract`),
  KEY `id_user` (`id_user`),
  KEY `id_vehicle` (`id_vehicle`),
  KEY `id_customer` (`id_customer`),
  KEY `id_voucher` (`id_voucher`),
  CONSTRAINT `contracts_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  CONSTRAINT `contracts_ibfk_2` FOREIGN KEY (`id_vehicle`) REFERENCES `vehicles` (`id_vehicle`),
  CONSTRAINT `contracts_ibfk_3` FOREIGN KEY (`id_customer`) REFERENCES `customers` (`id_customer`),
  CONSTRAINT `contracts_ibfk_4` FOREIGN KEY (`id_voucher`) REFERENCES `vouchers` (`id_voucher`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contracts`
--

LOCK TABLES `contracts` WRITE;
/*!40000 ALTER TABLE `contracts` DISABLE KEYS */;
INSERT INTO `contracts` VALUES (1,'HD-001','2026-01-10 08:00:00','2026-01-11 08:00:00','2026-01-11 09:00:00',1000,1150,100,80,'TIEN MAT',500000,150000,0,180000,'DA THANH TOAN','HOAN THANH',2,1,1,NULL),(2,'HD-002','2026-01-24 08:00:00','2026-01-26 08:00:00',NULL,3500,NULL,100,NULL,'GIAY TO',0,240000,24000,216000,'CHUA THANH TOAN','DANG THUE',2,2,2,2);
/*!40000 ALTER TABLE `contracts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customerdocuments`
--

DROP TABLE IF EXISTS `customerdocuments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customerdocuments` (
  `id_document` int NOT NULL,
  `id_customer` int NOT NULL,
  `document_type` enum('CCCD','BANG LAI','HO CHIEU') COLLATE utf8mb4_unicode_ci NOT NULL,
  `document_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_document`),
  KEY `id_customer` (`id_customer`),
  CONSTRAINT `customerdocuments_ibfk_1` FOREIGN KEY (`id_customer`) REFERENCES `customers` (`id_customer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `id_customer` int NOT NULL,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` tinyint(1) DEFAULT '1',
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `is_blacklist` tinyint(1) DEFAULT '0',
  `blacklist_reason` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_customer`),
  UNIQUE KEY `cccd` (`cccd`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'012345678901','Nguyễn Văn An',1,'0912000111','an.nguyen@gmail.com','Hòa Vang, Đà Nẵng',0,NULL),(2,'012345678902','Lê Thị Bình',0,'0912222333','binh.le@gmail.com','Sơn Trà, Đà Nẵng',0,NULL),(3,'012345678903','Phạm Văn Xấu',1,'0912444555','xau.pham@gmail.com','Quảng Nam',1,'Quỵt tiền thuê xe năm 2024');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inspections`
--

DROP TABLE IF EXISTS `inspections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inspections` (
  `id_inspection` int NOT NULL,
  `id_contract` int NOT NULL,
  `id_user` int NOT NULL,
  `inspection_type` enum('GIAO XE','TRA XE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_inspection`),
  KEY `id_contract` (`id_contract`),
  KEY `id_user` (`id_user`),
  CONSTRAINT `inspections_ibfk_1` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`),
  CONSTRAINT `inspections_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`)
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
  `id_part_price` int NOT NULL,
  `part_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehicle_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  PRIMARY KEY (`id_part_price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `partprices`
--

LOCK TABLES `partprices` WRITE;
/*!40000 ALTER TABLE `partprices` DISABLE KEYS */;
INSERT INTO `partprices` VALUES (1,'Gương chiếu hậu','Tay ga',150000),(2,'Gương chiếu hậu','Xe số',100000),(3,'Đèn pha','Honda',350000),(4,'Vỏ xe sau','Tất cả',250000);
/*!40000 ALTER TABLE `partprices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id_payment` int NOT NULL,
  `amount` double NOT NULL,
  `payment_type` enum('TIEN COC','THANH TOAN PHAN CON LAI','HOAN TIEN','PHU THU') COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_method` enum('TIEN MAT','CHUYEN KHOAN') COLLATE utf8mb4_unicode_ci DEFAULT 'TIEN MAT',
  `id_user` int NOT NULL,
  `id_contract` int NOT NULL,
  PRIMARY KEY (`id_payment`),
  KEY `id_user` (`id_user`),
  KEY `id_contract` (`id_contract`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `id_penalty` int NOT NULL,
  `id_contract` int NOT NULL,
  `penalty_type` enum('QUA GIO','XANG','HU HONG','VI PHAM GIAO THONG') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `amount` double DEFAULT NULL,
  PRIMARY KEY (`id_penalty`),
  KEY `id_contract` (`id_contract`),
  CONSTRAINT `penalties_ibfk_1` FOREIGN KEY (`id_contract`) REFERENCES `contracts` (`id_contract`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
-- Table structure for table `rules`
--

DROP TABLE IF EXISTS `rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rules` (
  `id_rule` int NOT NULL,
  `rule_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rule_type` enum('CUOI TUAN','NGAY LE','KHAC') COLLATE utf8mb4_unicode_ci NOT NULL,
  `multi` double NOT NULL,
  `star_date` date NOT NULL,
  `end_date` date NOT NULL,
  `is_active` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id_rule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
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
  `id_setting` int NOT NULL,
  `setting_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `setting_value` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_type` enum('STRING','NUMBER','BOOLEAN','JSON') COLLATE utf8mb4_unicode_ci DEFAULT 'STRING',
  `description` text COLLATE utf8mb4_unicode_ci,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` int NOT NULL,
  `update_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_setting`),
  UNIQUE KEY `setting_key` (`setting_key`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `systemsettings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `systemsettings`
--

LOCK TABLES `systemsettings` WRITE;
/*!40000 ALTER TABLE `systemsettings` DISABLE KEYS */;
INSERT INTO `systemsettings` VALUES (1,'Gia_Xang_Litre','25000','NUMBER','Giá xăng thị trường dùng để tính phạt','pricing',1,'2026-01-25 07:05:12'),(2,'Phi_Tre_Gio','100000','NUMBER','Phí phạt trễ mỗi giờ mặc định','penalty',1,'2026-01-25 07:05:12');
/*!40000 ALTER TABLE `systemsettings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id_user` int NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('Admin','Staff') COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` tinyint(1) DEFAULT '1',
  `birth_date` date DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `address` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `cccd` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin_quan','password123','Admin','Trần Văn Quản','0905123456','048099000123',1,'1990-05-15','quan.admin@gmail.com',1,'Hải Châu, Đà Nẵng'),(2,'staff_lan','staff789','Staff','Nguyễn Thị Lan','0905666777','048099000456',0,'1995-10-20','lan.staff@gmail.com',1,'Liên Chiểu, Đà Nẵng');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicles`
--

DROP TABLE IF EXISTS `vehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicles` (
  `id_vehicle` int NOT NULL,
  `code_vehicle` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `brand` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `year_of_manufacture` int DEFAULT NULL,
  `price_day` double DEFAULT NULL,
  `price_hour` double DEFAULT NULL,
  `fuel_capacity` int DEFAULT NULL,
  `current_km` int DEFAULT '0',
  `status` enum('AVAILABLE','RENTED','RESERVED','MAINTENANCE','INACTIVE') COLLATE utf8mb4_unicode_ci DEFAULT 'AVAILABLE',
  `total_price` double DEFAULT '0',
  PRIMARY KEY (`id_vehicle`),
  UNIQUE KEY `code_vehicle` (`code_vehicle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicles`
--

LOCK TABLES `vehicles` WRITE;
/*!40000 ALTER TABLE `vehicles` DISABLE KEYS */;
INSERT INTO `vehicles` VALUES (1,'43A-123.45','Honda','SH Mode','Trắng',2022,150000,20000,6,1200,'AVAILABLE',4500000),(2,'43B-678.90','Yamaha','Exciter 150','Xanh',2021,120000,15000,4,3500,'RENTED',2100000),(3,'43C-111.11','Honda','Wave Alpha','Đỏ',2023,100000,12000,4,500,'AVAILABLE',1000000),(4,'43D-222.22','SYM','Attila','Đen',2019,110000,14000,5,8000,'MAINTENANCE',500000);
/*!40000 ALTER TABLE `vehicles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id_voucher` int NOT NULL,
  `code_voucher` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `discount_type` enum('CO DINH','PHAN TRAM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_value` double DEFAULT NULL,
  `usage_limit` int DEFAULT '0',
  `usage_count` int DEFAULT '0',
  `valid_from_date` date NOT NULL,
  `valid_to_date` date NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id_voucher`),
  UNIQUE KEY `code_voucher` (`code_voucher`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` VALUES (1,'HELLO2026','Giảm 50k cho khách mới','CO DINH',50000,100,5,'2026-01-01','2026-12-31',1),(2,'TET2026','Giảm 10% dịp Tết','PHAN TRAM',10,50,0,'2026-01-20','2026-02-05',1);
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

-- Dump completed on 2026-01-26 15:12:09
