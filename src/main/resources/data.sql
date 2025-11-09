-- Insert admin user (password: admin)
INSERT INTO app_user (username, password, email, role, created_at, is_active) 
VALUES ('admin', 'admin', 'admin@smartquiz.com', 'ADMIN', NOW(), true)
ON CONFLICT (username) DO NOTHING;

-- Insert sample student user (password: student)
INSERT INTO app_user (username, password, email, role, created_at, is_active) 
VALUES ('student', 'student', 'student@smartquiz.com', 'STUDENT', NOW(), true)
ON CONFLICT (username) DO NOTHING;
