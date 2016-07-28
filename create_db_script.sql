CREATE DATABASE  IF NOT EXISTS `bttf` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `bttf`;
-- MySQL dump 10.13  Distrib 5.7.9, for Win64 (x86_64)
--
-- Host: localhost    Database: bttf
-- ------------------------------------------------------
-- Server version	5.7.11-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cycle`
--

DROP TABLE IF EXISTS `cycle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cycle` (
  `id_cycle` int(11) NOT NULL AUTO_INCREMENT,
  `id_project` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_cycle`),
  KEY `cycle_project_idx` (`id_project`),
  CONSTRAINT `cycle_project` FOREIGN KEY (`id_project`) REFERENCES `project` (`id_project`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cycle_elements`
--

DROP TABLE IF EXISTS `cycle_elements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cycle_elements` (
  `id_cycle_elements` int(11) NOT NULL AUTO_INCREMENT,
  `id_cycle` int(11) NOT NULL,
  `id_element` int(11) NOT NULL,
  PRIMARY KEY (`id_cycle_elements`),
  KEY `cycle_elements_cycle_idx` (`id_cycle`),
  KEY `cycle_elements_element_idx` (`id_element`),
  CONSTRAINT `cycle_elements_cycle` FOREIGN KEY (`id_cycle`) REFERENCES `cycle` (`id_cycle`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `cycle_elements_element` FOREIGN KEY (`id_element`) REFERENCES `element` (`id_element`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `element`
--

DROP TABLE IF EXISTS `element`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `element` (
  `id_element` int(11) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(150) NOT NULL,
  `type` varchar(50) NOT NULL,
  `code` text NOT NULL,
  `is_hook` tinyint(4) NOT NULL DEFAULT '0',
  `in_cycle` tinyint(4) NOT NULL DEFAULT '0',
  `is_fprivate` tinyint(4) NOT NULL DEFAULT '0',
  `is_fpublic` tinyint(4) NOT NULL DEFAULT '0',
  `id_project` int(11) NOT NULL,
  `id_feature` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_element`),
  KEY `element_project_idx` (`id_project`),
  KEY `element_feature_idx` (`id_feature`),
  CONSTRAINT `element_feature` FOREIGN KEY (`id_feature`) REFERENCES `feature` (`id_feature`) ON DELETE SET NULL ON UPDATE NO ACTION,
  CONSTRAINT `element_project` FOREIGN KEY (`id_project`) REFERENCES `project` (`id_project`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1741 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fact`
--

DROP TABLE IF EXISTS `fact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fact` (
  `id_fact` int(11) NOT NULL AUTO_INCREMENT,
  `fact` varchar(150) NOT NULL,
  `id_element_fact` int(11) NOT NULL,
  `elem_fact_isfprivate` tinyint(4) NOT NULL,
  `id_feature` int(11) NOT NULL,
  PRIMARY KEY (`id_fact`),
  KEY `fact_feature_idx` (`id_feature`),
  KEY `fact_element_idx` (`id_element_fact`),
  CONSTRAINT `fact_element` FOREIGN KEY (`id_element_fact`) REFERENCES `element` (`id_element`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fact_feature` FOREIGN KEY (`id_feature`) REFERENCES `feature` (`id_feature`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=767 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feature`
--

DROP TABLE IF EXISTS `feature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feature` (
  `id_feature` int(11) NOT NULL AUTO_INCREMENT,
  `feature_name` varchar(50) NOT NULL,
  `feature_order` int(11) NOT NULL,
  `id_feature_model` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_feature`),
  KEY `feature_feature_model_idx` (`id_feature_model`),
  CONSTRAINT `feature_feature_model` FOREIGN KEY (`id_feature_model`) REFERENCES `feature_model` (`id_feature_model`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feature_model`
--

DROP TABLE IF EXISTS `feature_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feature_model` (
  `id_feature_model` int(11) NOT NULL AUTO_INCREMENT,
  `feature_model` varchar(150) NOT NULL,
  `id_project` int(11) NOT NULL,
  PRIMARY KEY (`id_feature_model`),
  KEY `feature_model_project_idx` (`id_project`),
  CONSTRAINT `feature_model_project` FOREIGN KEY (`id_project`) REFERENCES `project` (`id_project`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inference`
--

DROP TABLE IF EXISTS `inference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inference` (
  `id_inference` int(11) NOT NULL AUTO_INCREMENT,
  `id_fact` int(11) NOT NULL,
  `inference` varchar(300) NOT NULL,
  `id_element` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_inference`),
  KEY `inference_fact_idx` (`id_fact`),
  KEY `inference_element_idx` (`id_element`),
  CONSTRAINT `inference_element` FOREIGN KEY (`id_element`) REFERENCES `element` (`id_element`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `inference_fact` FOREIGN KEY (`id_fact`) REFERENCES `fact` (`id_fact`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1376 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project` (
  `id_project` int(11) NOT NULL AUTO_INCREMENT,
  `project_name` varchar(100) NOT NULL,
  `language` varchar(45) NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id_project`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reference`
--

DROP TABLE IF EXISTS `reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reference` (
  `id_reference` int(11) NOT NULL AUTO_INCREMENT,
  `from_id_elem` int(11) NOT NULL,
  `to_id_elem` int(11) NOT NULL,
  `id_project` int(11) NOT NULL,
  PRIMARY KEY (`id_reference`),
  KEY `reference_element_idx` (`from_id_elem`),
  KEY `reference_element_to_idx` (`to_id_elem`),
  KEY `reference_project_idx` (`id_project`),
  CONSTRAINT `reference_element_from` FOREIGN KEY (`from_id_elem`) REFERENCES `element` (`id_element`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `reference_element_to` FOREIGN KEY (`to_id_elem`) REFERENCES `element` (`id_element`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `reference_project` FOREIGN KEY (`id_project`) REFERENCES `project` (`id_project`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=6232 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-04-06 14:59:25
