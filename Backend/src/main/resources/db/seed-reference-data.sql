-- ---------------------------------------------------------------------------
-- seed-reference-data.sql
--
-- The campuses and categories a listing needs before anyone can create one.
--
-- Hibernate generates the TABLES from the entities (ddl-auto=update), but it
-- never puts rows in them - so on a fresh database "Create listing" has an empty
-- Category and Campus dropdown and the form cannot be submitted. That makes the
-- whole marketplace look broken when it is only unseeded.
--
-- Run once against the uniexchange database:
--
--   mysql -u root -p uniexchange < src/main/resources/db/seed-reference-data.sql
--
-- Safe to run again: every statement is keyed on a unique column and does
-- nothing on a second run, so re-running after a teammate has added rows will
-- not duplicate or overwrite anything.
--
-- Deliberately NOT named data.sql. Spring Boot would then execute it on every
-- startup, which is fine for reference data but quietly surprising for anyone
-- who expects an empty database to stay empty.
--
-- Roles are not seeded here: AuthController creates the STUDENT role on demand
-- when the first account registers.
--
-- Author: Mogamat Yaseen Kannemeyer 240453182
-- Date: 24 September 2026
-- ---------------------------------------------------------------------------

-- Campuses ------------------------------------------------------------------
-- CPUT's main sites. `name` is not unique in the schema, so these are guarded
-- with a NOT EXISTS check rather than INSERT IGNORE.

-- Every column in the derived table needs an explicit alias. Without one MySQL
-- names the column after the literal, so a row whose name and city are both
-- 'Bellville' fails with "Duplicate column name".

INSERT INTO campus (name, city, address)
SELECT * FROM (SELECT 'Cape Town' AS c_name, 'Cape Town' AS c_city, 'Keizersgracht St, Zonnebloem' AS c_address) AS candidate
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'Cape Town');

INSERT INTO campus (name, city, address)
SELECT * FROM (SELECT 'Bellville' AS c_name, 'Bellville' AS c_city, 'Symphony Way, Bellville' AS c_address) AS candidate
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'Bellville');

INSERT INTO campus (name, city, address)
SELECT * FROM (SELECT 'Mowbray' AS c_name, 'Cape Town' AS c_city, 'Highbury Rd, Mowbray' AS c_address) AS candidate
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'Mowbray');

INSERT INTO campus (name, city, address)
SELECT * FROM (SELECT 'Wellington' AS c_name, 'Wellington' AS c_city, 'Church St, Wellington' AS c_address) AS candidate
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'Wellington');

INSERT INTO campus (name, city, address)
SELECT * FROM (SELECT 'Granger Bay' AS c_name, 'Cape Town' AS c_city, 'Beach Rd, Granger Bay' AS c_address) AS candidate
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'Granger Bay');

-- Categories ----------------------------------------------------------------
-- category.name IS unique, so INSERT IGNORE is enough here.

INSERT IGNORE INTO category (name, description) VALUES
  ('Textbooks',      'Course textbooks, study guides and past papers'),
  ('Electronics',    'Laptops, phones, calculators, chargers and accessories'),
  ('Stationery',     'Pens, notebooks, drawing equipment and lab supplies'),
  ('Furniture',      'Desks, chairs, shelves and residence furniture'),
  ('Clothing',       'Clothes, shoes and CPUT branded items'),
  ('Sports',         'Sports equipment, gym gear and bicycles'),
  ('Accommodation',  'Room shares, sublets and residence swaps'),
  ('Services',       'Tutoring, printing, repairs and lifts'),
  ('Other',          'Anything that does not fit the categories above');
