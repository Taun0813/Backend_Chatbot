UPDATE inventory
SET available_quantity = 100,
    updated_at = now()
WHERE product_id = 9;