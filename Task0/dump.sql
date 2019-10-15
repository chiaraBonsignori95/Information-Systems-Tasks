CREATE DATABASE  IF NOT EXISTS `hotel_chain` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `hotel_chain`;
-- MySQL dump 10.13  Distrib 8.0.17, for Win64 (x86_64)
--
-- Host: localhost    Database: hotel_chain
-- ------------------------------------------------------
-- Server version	8.0.17

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
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `surname` varchar(45) NOT NULL,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'Giorgio','Rossi','a','a'),(2,'Alessia','Cantini','b','b'),(3,'Lorenzo','Lorenzi','c','c'),(4,'Marco','Verdi','d','d'),(5,'Federico','Lossi','e','e'),(6,'Chiara','Bibi','f','f'),(7,'Alessio','Germani','g','g'),(8,'Roberto','Signorini','h','h'),(9,'Silvia','Morelli','i','i'),(10,'Sara','Picchi','l','l'),(11,'Sara','Fanno','m','m'),(12,'Alessio','Lanza','n','n'),(13,'Luca','Pinna','o','o'),(14,'Eugenio','Montale','p','p'),(15,'Chiara','Penna','q','q'),(16,'Mario','Geni','r','r'),(17,'Gerolamo','Savonarola','s','s');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotel`
--

DROP TABLE IF EXISTS `hotel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `street` varchar(45) NOT NULL,
  `number` int(11) NOT NULL,
  `city` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotel`
--

LOCK TABLES `hotel` WRITE;
/*!40000 ALTER TABLE `hotel` DISABLE KEYS */;
INSERT INTO `hotel` VALUES (1,'black street',167,'Rome'),(2,'orange street',148,'Milan'),(3,'yellow street',5,'London'),(4,'red street',34,'Chicago'),(5,'blue street',18,'Cincinnati'),(6,'green street',222,'Washington'),(7,'green street',17,'Bonston');
/*!40000 ALTER TABLE `hotel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receptionist`
--

DROP TABLE IF EXISTS `receptionist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receptionist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  `surname` varchar(45) NOT NULL,
  `hotel` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  KEY `fk_to_hotel_idx` (`hotel`),
  CONSTRAINT `fk_to_hotel` FOREIGN KEY (`hotel`) REFERENCES `hotel` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receptionist`
--

LOCK TABLES `receptionist` WRITE;
/*!40000 ALTER TABLE `receptionist` DISABLE KEYS */;
INSERT INTO `receptionist` VALUES (1,'z1','a','Manuela','Arcuri',1),(2,'z2','b','Gianni','Morandi',2),(3,'z3','c','Lapo','Lollo',3),(4,'z4','d','Cesare','Cesari',4),(5,'z5','e','Francesca','Franceschi',5),(6,'z6','f','Roberto','Roberti',6),(7,'z7','g','Luca','Luchi',7),(8,'z8','h','Lorenzo','Lorenzi',7);
/*!40000 ALTER TABLE `receptionist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `hotel` int(11) NOT NULL,
  `room` int(11) NOT NULL,
  `check-in date` date NOT NULL,
  `check-out date` date NOT NULL,
  `customer` int(11) NOT NULL,
  PRIMARY KEY (`hotel`,`room`,`check-in date`),
  KEY `hotel_idx` (`hotel`,`room`),
  KEY `fk_reservation_to_user_idx` (`customer`),
  CONSTRAINT `fk_to_customer` FOREIGN KEY (`customer`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_to_room` FOREIGN KEY (`hotel`, `room`) REFERENCES `room` (`hotel`, `number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
INSERT INTO `reservation` VALUES (1,1,'2019-10-08','2019-10-10',1),(1,2,'2019-10-24','2019-10-30',8),(2,1,'2019-10-09','2019-10-12',2),(2,2,'2019-10-10','2019-10-11',9),(3,1,'2019-10-12','2019-10-18',3),(3,2,'2019-10-12','2019-10-13',10),(4,1,'2019-10-15','2019-10-20',4),(4,2,'2019-10-09','2019-10-18',11),(5,1,'2019-10-18','2019-10-19',5),(5,2,'2019-09-08','2019-09-10',12),(6,1,'2019-10-21','2019-10-23',6),(6,2,'2019-09-10','2019-09-19',13),(7,1,'2019-10-22','2019-10-29',7),(7,2,'2019-09-01','2019-09-03',14);
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room` (
  `hotel` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `capacity` int(11) NOT NULL,
  PRIMARY KEY (`hotel`,`number`),
  CONSTRAINT `id` FOREIGN KEY (`hotel`) REFERENCES `hotel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */;
INSERT INTO `room` VALUES (1,1,2),(1,2,4),(1,3,4),(1,4,2),(1,5,2),(1,6,4),(2,1,2),(2,2,4),(2,3,2),(2,4,2),(2,5,2),(2,6,2),(3,1,2),(3,2,2),(3,3,4),(3,4,4),(3,5,1),(3,6,2),(3,7,1),(4,1,2),(4,2,4),(4,3,2),(4,4,2),(4,5,4),(4,6,2),(4,7,4),(4,8,4),(4,9,2),(5,1,2),(5,2,2),(5,3,1),(5,4,4),(5,5,4),(5,6,2),(5,7,4),(5,8,1),(5,9,2),(6,1,2),(6,2,4),(6,3,2),(6,4,2),(6,5,2),(6,6,4),(6,7,4),(6,8,4),(6,9,4),(7,1,4),(7,2,4),(7,3,2),(7,4,2),(7,5,4),(7,6,1),(7,7,2),(7,8,2),(7,9,4),(7,10,4),(7,11,2),(7,12,2),(7,13,2),(7,14,2),(7,15,1),(7,16,2),(7,17,4),(7,18,4),(7,19,2);
/*!40000 ALTER TABLE `room` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-10-14 18:19:14
