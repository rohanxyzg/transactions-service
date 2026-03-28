INSERT INTO operation_types (operation_type_id, description, is_credit)
SELECT 1, 'Normal Purchase', false
WHERE NOT EXISTS (SELECT 1 FROM operation_types WHERE operation_type_id = 1);

INSERT INTO operation_types (operation_type_id, description, is_credit)
SELECT 2, 'Purchase with installments', false
WHERE NOT EXISTS (SELECT 1 FROM operation_types WHERE operation_type_id = 2);

INSERT INTO operation_types (operation_type_id, description, is_credit)
SELECT 3, 'Withdrawal', false
WHERE NOT EXISTS (SELECT 1 FROM operation_types WHERE operation_type_id = 3);

INSERT INTO operation_types (operation_type_id, description, is_credit)
SELECT 4, 'Credit Voucher', true
WHERE NOT EXISTS (SELECT 1 FROM operation_types WHERE operation_type_id = 4);
