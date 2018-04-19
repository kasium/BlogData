/*
	CAUTION: A schema with the name "WEBSHOP" is used for all operations. Please make sure that no schema with this name exists on your system!
	Please execute the blocks one by one. Block staring with ACTION requires additional actions from user side.
	
	Author: Kai Mueller
	Supported SAP HANA Versions: SAP HANA 2.0 SPS2 and SPS3
*/

-- Create schema
CREATE SCHEMA WEBSHOP;
SET SCHEMA WEBSHOP;

-- Create tables
CREATE COLUMN TABLE WEBSHOP."Product" (
	ID INT PRIMARY KEY, 
	price DOUBLE NOT NULL, 
	Name NVARCHAR(1000)  NOT NULL, 
	Category NVARCHAR(1000) NOT NULL
);
CREATE COLUMN TABLE WEBSHOP."Book" (
	ID INT PRIMARY KEY, 
	Author NVARCHAR(1000) NOT NULL, 
	Pages INT NOT NULL, 
	Edition INT NOT NULL,
	FOREIGN KEY(ID) REFERENCES WEBSHOP."Product"
);
CREATE COLUMN TABLE WEBSHOP."CD" (
	ID INT PRIMARY KEY, 
	Artist NVARCHAR(1000) NOT NULL, 
	AlbumName NVARCHAR(1000) NOT NULL, 
	Genre NVARCHAR(1000) NOT NULL,
	FOREIGN KEY(ID) REFERENCES WEBSHOP."Product"
);
CREATE COLUMN TABLE WEBSHOP."Customer" (
	ID INT PRIMARY KEY,
	Name NVARCHAR(1000) NOT NULL, 
	Address NVARCHAR(1000) NOT NULL
);
CREATE COLUMN TABLE WEBSHOP."Order" (
	ID INT PRIMARY KEY, 
	CustomerID INT NOT NULL, 
	OrderDate DATE NOT NULL,
	FOREIGN KEY(CustomerID) REFERENCES WEBSHOP."Customer"
);
CREATE COLUMN TABLE WEBSHOP."OrderPosition" (
	ID INT PRIMARY KEY,
	OrderID INT NOT NULL,
	ProductID INT NOT NULL,
	Amount INT NOT NULL,
	FOREIGN KEY(OrderID) REFERENCES WEBSHOP."Order",
	FOREIGN KEY(ProductID) REFERENCES WEBSHOP."Product"
);

-- ACTION Import Data from data/ProductData.sql
-- ACTION Import Data from data/BookData.sql
-- ACTION Import Data from data/CDData.sql
-- ACTION Import Data from data/CustomerData.sql
-- ACTION Import Data from data/OrderData.sql
-- ACTION Import Data from data/OrderPositionData.sql

-- Create collection
CREATE COLLECTION WEBSHOP."ProductCollection";

-- Import data for the collection by using the tables
INSERT INTO WEBSHOP."ProductCollection"
	SELECT '{"id":' || P.ID || 
	       ',"price":' || P.Price || 
		   ',"name":"' || P.Name || 
		   '","author":"' || B.Author || 
		   '", "pages":' || B.Pages || 
		   ', "edition":' || B.Edition || 
		   '}' AS JSON
	FROM WEBSHOP."Product" P INNER JOIN WEBSHOP."Book" B ON P.ID = B.ID;
INSERT INTO WEBSHOP."ProductCollection"
	SELECT '{"id":' || P.ID || 
	       ',"price":' || P.Price || 
		   ',"name":"' || P.Name || 
		   '","artist":"' || C.Artist || 
		   '", "albumname":"' || C.AlbumName || 
		   '", "genre":"' || C.Genre || 
		   '"}' AS JSON
	FROM WEBSHOP."Product" P INNER JOIN WEBSHOP."CD" C ON P.ID = C.ID;