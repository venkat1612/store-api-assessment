-- Create product table
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

-- Insert product names from existing orders into the product table
CREATE TABLE order_product (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT pk_order_product PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_product_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_product_product_id ON order_product (product_id);
CREATE INDEX idx_order_customer_id ON "order" (customer_id);

-- Preserve existing orders as one-product orders by treating their legacy description as the product description.
INSERT INTO product (description)
SELECT DISTINCT description
FROM "order"
WHERE description IS NOT NULL;

INSERT INTO order_product (order_id, product_id)
SELECT o.id, p.id
FROM "order" o
JOIN product p ON p.description = o.description;
