INSERT INTO operation_types (operation_type_id, description, is_credit) VALUES
    (1, 'Normal Purchase',            false),
    (2, 'Purchase with installments', false),
    (3, 'Withdrawal',                 false),
    (4, 'Credit Voucher',             true)
ON CONFLICT (operation_type_id) DO NOTHING;
