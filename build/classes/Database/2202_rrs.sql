-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 11, 2025 at 05:56 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `2202_rrs`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetAllCustomers` ()   BEGIN
	SELECT COUNT(*) AS total_customers FROM customers;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetCustomerGrowth` ()   BEGIN
    WITH last_week AS (
        SELECT COUNT(*) AS customers_last_week
        FROM customers
        WHERE WEEK(created_at, 1) = WEEK(CURDATE(), 1) - 1
        AND YEAR(created_at) = YEAR(CURDATE())
    ),
    current_week AS (
        SELECT COUNT(*) AS customers_this_week
        FROM customers
        WHERE WEEK(created_at, 1) = WEEK(CURDATE(), 1)
        AND YEAR(created_at) = YEAR(CURDATE())
    )
    
    SELECT 
        current_week.customers_this_week,
        last_week.customers_last_week,
        CASE 
            WHEN last_week.customers_last_week = 0 AND current_week.customers_this_week = 0 THEN 0
            WHEN last_week.customers_last_week = 0 THEN 100
            ELSE ((current_week.customers_this_week - last_week.customers_last_week) / last_week.customers_last_week) * 100
        END AS customer_growth_percentage
    FROM current_week, last_week;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetNewCustomers` ()   BEGIN
	SELECT COUNT(*) AS new_customers 
FROM customers 
WHERE WEEK(created_at, 1) = WEEK(CURDATE(), 1) 
AND YEAR(created_at) = YEAR(CURDATE());
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetNewOrders` ()   BEGIN
	SELECT COUNT(*) AS new_orders 
FROM orders 
WHERE WEEK(order_date, 1) = WEEK(CURDATE(), 1) 
AND YEAR(order_date) = YEAR(CURDATE());
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetNewRefundedOrders` ()   BEGIN
	SELECT SUM(total_price) AS refunded_this_week 
    FROM orders 
    WHERE status = 'Refunded' 
    AND WEEK(order_date, 1) = WEEK(CURDATE(), 1) 
    AND YEAR(order_date) = YEAR(CURDATE());
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetNewSales` ()   BEGIN
	SELECT COALESCE(SUM(total_price), 0) AS sales_this_week
	FROM orders
	WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetOrderGrowth` ()   BEGIN
    WITH last_week AS (
        SELECT COUNT(*) AS orders_last_week
        FROM orders
        WHERE WEEK(order_date, 1) = WEEK(CURDATE(), 1) - 1
        AND YEAR(order_date) = YEAR(CURDATE())
    ),
    current_week AS (
        SELECT COUNT(*) AS orders_this_week
        FROM orders
        WHERE WEEK(order_date, 1) = WEEK(CURDATE(), 1)
        AND YEAR(order_date) = YEAR(CURDATE())
    )
    
    SELECT 
        current_week.orders_this_week,
        last_week.orders_last_week,
        CASE 
            WHEN last_week.orders_last_week = 0 AND current_week.orders_this_week = 0 THEN 0
            WHEN last_week.orders_last_week = 0 THEN 100
            ELSE ((current_week.orders_this_week - last_week.orders_last_week) / last_week.orders_last_week) * 100
        END AS order_growth_percentage
    FROM current_week, last_week;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetRefundedOrderGrowth` ()   BEGIN
	WITH previous_avg AS (
    SELECT IFNULL(AVG(weekly_refunds), 0) AS avg_past_refunds
    FROM (
        SELECT SUM(total_price) AS weekly_refunds 
        FROM orders 
        WHERE status = 'Refunded' 
        AND WEEK(order_date, 1) < WEEK(CURDATE(), 1) 
        AND YEAR(order_date) = YEAR(CURDATE())
        GROUP BY WEEK(order_date, 1)
    ) AS past_weeks
),
current_week AS (
    SELECT SUM(total_price) AS refunds_this_week 
    FROM orders 
    WHERE status = 'Refunded' 
    AND WEEK(order_date, 1) = WEEK(CURDATE(), 1) 
    AND YEAR(order_date) = YEAR(CURDATE())
)
SELECT 
    current_week.refunds_this_week,
    previous_avg.avg_past_refunds,
    CASE 
        WHEN previous_avg.avg_past_refunds = 0 AND current_week.refunds_this_week = 0 THEN 0
        WHEN previous_avg.avg_past_refunds = 0 THEN 100
        ELSE ((current_week.refunds_this_week - previous_avg.avg_past_refunds) / previous_avg.avg_past_refunds) * 100
    END AS refund_growth_percentage
FROM current_week, previous_avg;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetRefundedOrders` ()   BEGIN
	SELECT SUM(total_price) AS total_refunded FROM orders WHERE status = 'Refunded';
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetRevenueStats` (IN `period` VARCHAR(10))   BEGIN
    DECLARE start_date DATE;
    DECLARE format_string VARCHAR(20);

    -- Determine the start date and format string based on period
    IF period = 'This week' THEN
        SET start_date = DATE_SUB(CURDATE(), INTERVAL 6 DAY);
        SET format_string = '%W';  -- Full weekday name (Monday, Tuesday, etc.)
    ELSEIF period = 'This month' THEN
        SET start_date = DATE_SUB(CURDATE(), INTERVAL 1 MONTH);
        SET format_string = '%d %b';  -- Day and short month (e.g., "05 Jan")
    ELSE
        SET start_date = DATE_SUB(CURDATE(), INTERVAL 11 MONTH);
        SET format_string = '%M';  -- Full month name (e.g., "January")
    END IF;

    -- Retrieve revenue statistics with formatted dates
    SELECT 
        DATE_FORMAT(o.order_date, format_string) AS date_label, 
        SUM(o.total_price) AS total_sales
    FROM orders o
    WHERE o.order_date >= start_date  -- Retrieve data from past period
    GROUP BY date_label
    ORDER BY MIN(o.order_date);  -- Ensures correct chronological order
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetSalesByCategory` (IN `period` VARCHAR(15))   BEGIN
    DECLARE date_condition VARCHAR(20);

    -- Set the correct date format condition
    IF period = 'This week' THEN
        SET date_condition = DATE_FORMAT(CURDATE(), '%Y-%u');
    ELSEIF period = 'This month' THEN
        SET date_condition = DATE_FORMAT(CURDATE(), '%Y-%m');
    ELSE
        SET date_condition = DATE_FORMAT(CURDATE(), '%Y');
    END IF;

    -- Query to get total sales per category
    SELECT 
        m.category AS category_name, 
        SUM(o.total_price) AS total_sales
    FROM orders o
    INNER JOIN order_items oi ON oi.order_id = o.order_id
    INNER JOIN menu m ON oi.menu_id = m.menu_id
    WHERE DATE_FORMAT(o.order_date, 
        CASE period 
            WHEN 'This week' THEN '%Y-%u'
            WHEN 'This month' THEN '%Y-%m'
            ELSE '%Y'
        END
    ) = date_condition
    GROUP BY category_name
    ORDER BY total_sales DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetSalesGrowth` ()   BEGIN
WITH previous_avg AS (
    SELECT IFNULL(AVG(weekly_sales), 0) AS avg_past_sales
    FROM (
        SELECT SUM(total_price) AS weekly_sales 
        FROM orders 
        WHERE WEEK(order_date, 1) < WEEK(CURDATE(), 1) 
        AND YEAR(order_date) = YEAR(CURDATE())
        GROUP BY WEEK(order_date, 1)
    ) AS past_weeks
),
current_week AS (
    SELECT IFNULL(SUM(total_price), 0) AS sales_this_week 
    FROM orders 
    WHERE WEEK(order_date, 1) = WEEK(CURDATE(), 1) 
    AND YEAR(order_date) = YEAR(CURDATE())
)
SELECT 
    current_week.sales_this_week,
    previous_avg.avg_past_sales,
    CASE 
        WHEN previous_avg.avg_past_sales = 0 AND current_week.sales_this_week = 0 THEN 0
        WHEN previous_avg.avg_past_sales = 0 THEN 100
        ELSE ((current_week.sales_this_week - previous_avg.avg_past_sales) / previous_avg.avg_past_sales) * 100
    END AS sales_growth_percentage
FROM current_week, previous_avg;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetTotalOrders` ()   BEGIN
	SELECT COUNT(*) AS total_orders FROM orders;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetTotalSales` ()   BEGIN
	SELECT COALESCE(SUM(total_price), 0) AS total_sales
	FROM orders;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `bundles`
--

CREATE TABLE `bundles` (
  `bundle_id` int(11) NOT NULL,
  `bundle_name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `image_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bundles`
--

INSERT INTO `bundles` (`bundle_id`, `bundle_name`, `price`, `image_path`) VALUES
(1, 'Filipino Feast', 699.00, 'Filipino-Feast\r\n'),
(2, 'Merienda Set', 399.00, NULL),
(3, 'Barkada Meal', 579.00, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `bundle_items`
--

CREATE TABLE `bundle_items` (
  `bundle_item_id` int(11) NOT NULL,
  `bundle_id` int(11) DEFAULT NULL,
  `menu_id` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bundle_items`
--

INSERT INTO `bundle_items` (`bundle_item_id`, `bundle_id`, `menu_id`, `quantity`) VALUES
(1, 1, 13, 3),
(2, 1, 14, 1),
(3, 1, 2, 2),
(4, 2, 4, 2),
(5, 2, 5, 2),
(6, 2, 10, 4),
(7, 3, 1, 2),
(8, 3, 15, 1),
(9, 3, 3, 3);

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `first_name`, `last_name`, `email`, `phone`, `created_at`) VALUES
(1, 'John', 'Doe', 'john.doe@example.com', '1234567890', '2025-03-10 06:51:39'),
(2, 'Jane', 'Smith', 'jane.smith@example.com', '0987654321', '2025-03-10 06:51:39'),
(3, 'Juan', 'Dela Cruz', 'juan.delacruz@email.com', '09171234567', '2025-03-10 10:11:31'),
(4, 'Maria', 'Santos', 'maria.santos@email.com', '09281234568', '2025-03-10 10:11:31'),
(5, 'Pedro', 'Reyes', 'pedro.reyes@email.com', '09391234569', '2025-03-10 10:11:31'),
(6, 'Ana', 'Lopez', 'ana.lopez@email.com', '09471234570', '2025-03-10 10:11:31'),
(7, 'Carlos', 'Gonzalez', 'carlos.gonzalez@email.com', '09581234571', '2025-03-10 10:11:31'),
(13, 'Linux', 'Adona', 'linuxadona@gmail.com', '09663340617', '2025-03-10 18:16:39'),
(14, 'John Dave', 'Briones', 'davebriones@gmail.com', '09123654789', '2025-03-11 13:52:35'),
(15, 'Jan Emmanuel', 'Formentos', 'jeformentos@gmail.com', '09987654321', '2025-03-11 15:39:28'),
(16, 'Millan', 'Abrenica', 'millan@gmail.com', '09123654987', '2025-03-11 16:26:55');

-- --------------------------------------------------------

--
-- Table structure for table `menu`
--

CREATE TABLE `menu` (
  `menu_id` int(11) NOT NULL,
  `item_name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `category` enum('Appetizer','Main Course','Dessert','Beverage') NOT NULL,
  `availability` tinyint(1) DEFAULT 1,
  `image_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `menu`
--

INSERT INTO `menu` (`menu_id`, `item_name`, `description`, `price`, `category`, `availability`, `image_path`) VALUES
(1, 'Pork Adobo', 'Braised chicken or pork in vinegar, soy sauce, and garlic', 149.00, 'Main Course', 1, 'Adobo'),
(2, 'Sinigang', 'Sour tamarind-based soup with pork and vegetables', 119.00, 'Main Course', 1, 'Sinigang\r\n'),
(3, 'Lumpiang Shanghai', 'Crispy spring rolls filled with ground pork and vegetables', 89.00, 'Appetizer', 1, NULL),
(4, 'Halo-Halo', 'Mixed shaved ice dessert with sweet beans, jellies, and ice cream', 79.00, 'Dessert', 1, NULL),
(5, 'Leche Flan', 'Caramel custard dessert', 69.00, 'Dessert', 1, NULL),
(9, 'Buko Juice', 'Refreshing coconut water served chilled', 49.00, 'Beverage', 1, NULL),
(10, 'Sago\'t Gulaman', 'Sweet and refreshing drink with tapioca pearls and gulaman', 49.00, 'Beverage', 1, NULL),
(11, 'Barako Coffee', 'Strong and bold Batangas coffee', 59.00, 'Beverage', 1, NULL),
(12, 'Calamansi Juice', 'Tangy and refreshing citrus drink', 49.00, 'Beverage', 1, NULL),
(13, 'Chicken Adobo', 'Chicken stewed in soy sauce, vinegar, and garlic', 149.00, 'Main Course', 1, 'Chicken Adobo\r\n'),
(14, 'Kare-Kare', 'Peanut-based stew with oxtail and vegetables', 179.00, 'Main Course', 1, 'Kare-Kare'),
(15, 'Pancit Canton', 'Stir-fried noodles with vegetables and meat', 109.00, 'Main Course', 1, NULL),
(16, 'Manggang Hilaw', 'Green mango slices with bagoong (fermented shrimp paste)', 99.00, 'Appetizer', 1, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `reservation_id` int(11) DEFAULT NULL,
  `customer_id` int(11) NOT NULL,
  `table_id` int(11) NOT NULL,
  `order_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('Pending','Preparing','Served','Cancelled','Completed','Refunded') DEFAULT 'Pending',
  `total_price` decimal(10,2) DEFAULT 0.00,
  `payment` decimal(10,2) NOT NULL DEFAULT 0.00,
  `change_due` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `reservation_id`, `customer_id`, `table_id`, `order_date`, `status`, `total_price`, `payment`, `change_due`) VALUES
(3, 5, 1, 4, '2025-03-10 06:59:28', 'Pending', 309.00, 309.00, 0.00),
(4, 6, 2, 5, '2025-03-10 06:59:28', 'Pending', 138.00, 138.00, 0.00),
(5, 7, 1, 4, '2025-03-01 04:15:00', 'Completed', 287.00, 287.00, 0.00),
(6, 8, 2, 5, '2025-03-03 03:45:00', 'Completed', 407.00, 407.00, 0.00),
(7, 9, 3, 6, '2025-03-02 08:20:00', 'Pending', 197.00, 197.00, 0.00),
(12, NULL, 13, 4, '2025-03-10 18:16:40', 'Refunded', 109.00, 109.00, 0.00),
(13, NULL, 14, 6, '2025-03-11 13:52:47', 'Pending', 149.00, 150.00, 1.00),
(14, NULL, 15, 4, '2025-03-11 15:39:39', 'Pending', 328.00, 330.00, 2.00),
(15, 12, 16, 4, '2025-03-11 16:26:55', 'Pending', 258.00, 100.00, 22.60);

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `menu_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_items`
--

INSERT INTO `order_items` (`order_item_id`, `order_id`, `menu_id`, `quantity`, `price`) VALUES
(1, 3, 2, 1, 119.00),
(2, 3, 3, 1, 89.00),
(3, 4, 3, 1, 89.00),
(4, 4, 9, 1, 49.00),
(14, 5, 3, 2, 89.00),
(15, 5, 13, 1, 149.00),
(16, 5, 10, 2, 49.00),
(17, 6, 2, 1, 119.00),
(18, 6, 14, 1, 179.00),
(19, 6, 16, 2, 109.00),
(20, 7, 5, 1, 69.00),
(21, 7, 4, 1, 79.00),
(22, 7, 9, 3, 49.00),
(24, 12, 15, 1, 109.00),
(25, 13, 13, 1, 149.00),
(26, 14, 13, 1, 149.00),
(27, 14, 14, 1, 179.00),
(28, 15, 13, 1, 149.00),
(29, 15, 15, 1, 109.00);

-- --------------------------------------------------------

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `reservation_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `table_id` int(11) NOT NULL,
  `reservation_time` datetime DEFAULT NULL,
  `guests` int(11) NOT NULL,
  `status` enum('Pending','Confirmed','Completed','Cancelled') DEFAULT 'Pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `down_payment` decimal(10,2) NOT NULL DEFAULT 500.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservations`
--

INSERT INTO `reservations` (`reservation_id`, `customer_id`, `table_id`, `reservation_time`, `guests`, `status`, `created_at`, `down_payment`) VALUES
(5, 1, 4, '2025-03-10 18:00:00', 5, 'Pending', '2025-03-10 06:58:28', 500.00),
(6, 2, 5, '2025-03-10 19:00:00', 4, 'Pending', '2025-03-10 06:58:28', 500.00),
(7, 1, 4, '2025-11-01 12:00:00', 4, 'Confirmed', '2025-03-10 10:11:31', 500.00),
(8, 2, 5, '2025-11-05 11:30:00', 2, 'Completed', '2025-03-10 10:11:31', 500.00),
(9, 3, 6, '2025-11-10 16:00:00', 3, 'Pending', '2025-03-10 10:11:31', 500.00),
(12, 16, 4, '2025-03-20 10:00:00', 1, 'Pending', '2025-03-11 16:26:55', 77.40);

-- --------------------------------------------------------

--
-- Table structure for table `tables`
--

CREATE TABLE `tables` (
  `table_id` int(11) NOT NULL,
  `capacity` int(11) NOT NULL,
  `status` enum('Available','Occupied') NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tables`
--

INSERT INTO `tables` (`table_id`, `capacity`, `status`) VALUES
(4, 6, 'Occupied'),
(5, 4, 'Available'),
(6, 2, 'Available');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `first_name`, `last_name`) VALUES
(1, 'linxad', 'linx123', 'Linux', 'Adona');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bundles`
--
ALTER TABLE `bundles`
  ADD PRIMARY KEY (`bundle_id`);

--
-- Indexes for table `bundle_items`
--
ALTER TABLE `bundle_items`
  ADD PRIMARY KEY (`bundle_item_id`),
  ADD KEY `menu_id` (`menu_id`),
  ADD KEY `bundle_items_ibfk_1` (`bundle_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone` (`phone`);

--
-- Indexes for table `menu`
--
ALTER TABLE `menu`
  ADD PRIMARY KEY (`menu_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `reservation_id` (`reservation_id`),
  ADD KEY `orders_ibfk_2` (`customer_id`),
  ADD KEY `orders_ibfk_3` (`table_id`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `menu_id` (`menu_id`);

--
-- Indexes for table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`reservation_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `table_id` (`table_id`);

--
-- Indexes for table `tables`
--
ALTER TABLE `tables`
  ADD PRIMARY KEY (`table_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bundles`
--
ALTER TABLE `bundles`
  MODIFY `bundle_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `bundle_items`
--
ALTER TABLE `bundle_items`
  MODIFY `bundle_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `menu`
--
ALTER TABLE `menu`
  MODIFY `menu_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `reservation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `tables`
--
ALTER TABLE `tables`
  MODIFY `table_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bundle_items`
--
ALTER TABLE `bundle_items`
  ADD CONSTRAINT `bundle_items_ibfk_1` FOREIGN KEY (`bundle_id`) REFERENCES `bundles` (`bundle_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bundle_items_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`menu_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`),
  ADD CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`table_id`) REFERENCES `tables` (`table_id`);

--
-- Constraints for table `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`menu_id`) ON DELETE CASCADE;

--
-- Constraints for table `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`table_id`) REFERENCES `tables` (`table_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
