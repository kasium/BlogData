/*
	This file contains all sample queries from the blog and some additional ones.
	
	Author: Kai Mueller
	Supported SAP HANA Versions: SAP HANA 2.0 SPS3
*/

-- TABLE: get all customers which bought 'Product 1' and the price they payed
SELECT C.Name, O.OrderDate, Amount * Price AS "PAYED" FROM WEBSHOP."Customer" C
	JOIN WEBSHOP."Order" O On O.CustomerID = C.ID
	JOIN WEBSHOP."OrderPosition" OP ON OP.OrderID = O.ID
	JOIN WEBSHOP."Product" P ON P.ID = OP.ProductID
WHERE P.Name = 'Product 1';

-- COLLECTION: get all customers which bought 'Product 1' and the price they payed
WITH productView AS (SELECT "id", "name", "price" FROM WEBSHOP."ProductCollection" WHERE "name" = 'Product 1')
	SELECT C.Name, O.OrderDate, Amount * TO_DOUBLE("price") AS "PAYED" FROM "Customer" C
		JOIN WEBSHOP."Order" O On O.CustomerID = C.ID
		JOIN WEBSHOP."OrderPosition" OP ON OP.OrderID = O.ID
		JOIN productView P ON P."id" = OP.ProductID;

-- TABLE: get favourite genre the customer with the ID 2
SELECT TOP 1 CD.Genre, COUNT(*) AS "COUNT" FROM WEBSHOP."Customer" C
	JOIN WEBSHOP."Order" O On O.CustomerID = C.ID
	JOIN WEBSHOP."OrderPosition" OP ON OP.OrderID = O.ID
	JOIN WEBSHOP."CD" CD ON CD.ID = OP.ProductID
WHERE C.ID = '2'
GROUP BY CD.Genre
ORDER BY COUNT DESC;

-- COLLECTION: get favourite genre the customer with the ID 2
WITH productView AS (SELECT "id", "genre" FROM WEBSHOP."ProductCollection" WHERE "genre" IS SET)
	SELECT TOP 1 P."genre", COUNT(*) AS "COUNT" FROM WEBSHOP."Customer" C
		JOIN WEBSHOP."Order" O On O.CustomerID = C.ID
		JOIN WEBSHOP."OrderPosition" OP ON OP.OrderID = O.ID
		JOIN productView P ON P."id" = OP.ProductID
	WHERE C.ID = '2'
	GROUP BY P."genre"
	ORDER BY COUNT DESC;
	
-- COLLECTION: create a new JSON on the fly with the SELECT command
SELECT {"productID": "id", "articleName": "name", "articlePrice": "price", "articleType": 'book'} FROM WEBSHOP."ProductCollection" WHERE "pages" IS SET;

-- COLLECTION: add a new field "authorDetails" to all documents with the auhor "Ollie Nesey"
UPDATE WEBSHOP."ProductCollection" SET "authorDetails" = 'This author is special' WHERE "author" = 'Ollie Nesey';

-- COLLECTION: remove the field "albumname" from all documents
UPDATE WEBSHOP."ProductCollection" UNSET "albumname";
