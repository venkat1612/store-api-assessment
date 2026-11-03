SELECT setval(
    pg_get_serial_sequence('customer', 'id'),
    COALESCE((SELECT MAX(id) FROM customer), 1),
    EXISTS (SELECT 1 FROM customer)
);

SELECT setval(
    pg_get_serial_sequence('"order"', 'id'),
    COALESCE((SELECT MAX(id) FROM "order"), 1),
    EXISTS (SELECT 1 FROM "order")
);

SELECT setval(
    pg_get_serial_sequence('product', 'id'),
    COALESCE((SELECT MAX(id) FROM product), 1),
    EXISTS (SELECT 1 FROM product)
);
