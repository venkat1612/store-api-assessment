-- Create product table
CREATE TABLE IF NOT EXISTS product (
                          id BIGSERIAL PRIMARY KEY,
                          description VARCHAR(255) NOT NULL
);

-- Insert product names from existing orders into the product table
INSERT INTO product (description)
SELECT DISTINCT o.description
FROM "order" o
WHERE o.description IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.description = o.description);

