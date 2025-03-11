-- Roles
INSERT INTO roles (name) VALUES ('ROLE_USER') ON DUPLICATE KEY UPDATE name = VALUES(name);
INSERT INTO roles (name) VALUES ('ROLE_PHARMACIST') ON DUPLICATE KEY UPDATE name = VALUES(name);
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Admin user (password: admin123)
INSERT INTO users (username, email, password, full_name, enabled, account_non_locked, created_at)
VALUES ('admin', 'admin@pharmacy.com', '$2a$10$ixlPY3AAd4ty1l6E2IsQ9OFZi2ba9ZQE0bP7RFcGIWNhyFrrT3YUi', 'Admin User', true, true, NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Admin role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), role_id = VALUES(role_id);

-- Sample suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address, active, created_at)
VALUES 
('ABC Pharmaceuticals', 'John Smith', '+1234567890', 'john@abcpharma.com', '123 Pharma St, Medical City', true, NOW()),
('MediSupply Inc.', 'Jane Doe', '+0987654321', 'jane@medisupply.com', '456 Health Ave, Wellness Town', true, NOW()),
('Global Meds', 'Robert Johnson', '+1122334455', 'robert@globalmeds.com', '789 Medicine Blvd, Care City', true, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Sample medicines
INSERT INTO medicines (name, description, category, price, stock_quantity, expiry_date, manufacturer, dosage, active, created_at, supplier_id)
VALUES 
('Paracetamol', 'Pain reliever and fever reducer', 'Painkillers', 5.99, 100, '2024-12-31', 'ABC Pharma', '500mg', true, NOW(), 1),
('Amoxicillin', 'Antibiotic used to treat bacterial infections', 'Antibiotics', 12.50, 50, '2024-06-30', 'MediSupply', '250mg', true, NOW(), 2),
('Vitamin C', 'Dietary supplement', 'Vitamins', 8.75, 200, '2025-01-15', 'Global Meds', '1000mg', true, NOW(), 3),
('Ibuprofen', 'Nonsteroidal anti-inflammatory drug', 'Painkillers', 6.25, 75, '2024-09-20', 'ABC Pharma', '200mg', true, NOW(), 1),
('Cetirizine', 'Antihistamine for allergy relief', 'Antihistamines', 9.99, 60, '2024-08-10', 'MediSupply', '10mg', true, NOW(), 2),
('Omeprazole', 'Proton pump inhibitor for acid reflux', 'Antacids', 15.50, 40, '2024-07-25', 'Global Meds', '20mg', true, NOW(), 3),
('Aspirin', 'Blood thinner and pain reliever', 'Painkillers', 4.50, 120, '2024-11-05', 'ABC Pharma', '81mg', true, NOW(), 1),
('Loratadine', 'Non-drowsy antihistamine', 'Antihistamines', 7.25, 90, '2024-10-15', 'MediSupply', '10mg', true, NOW(), 2),
('Multivitamin', 'Daily vitamin supplement', 'Vitamins', 12.99, 150, '2025-02-28', 'Global Meds', 'Once daily', true, NOW(), 3),
('Diclofenac', 'Anti-inflammatory pain reliever', 'Painkillers', 10.75, 30, '2024-05-20', 'ABC Pharma', '50mg', true, NOW(), 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

