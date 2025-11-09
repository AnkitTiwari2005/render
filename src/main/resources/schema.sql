-- Drop tables if they exist (clean start)
DROP TABLE IF EXISTS attempt CASCADE;
DROP TABLE IF EXISTS question CASCADE;
DROP TABLE IF EXISTS quiz CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

-- Create app_user table (renamed from user)
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    role VARCHAR(10) NOT NULL,
    created_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Create quiz table
CREATE TABLE quiz (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_seconds INTEGER DEFAULT 300,
    is_published BOOLEAN DEFAULT false,
    created_by BIGINT NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create question table
CREATE TABLE question (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quiz(id),
    text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT,
    option_d TEXT,
    correct_option VARCHAR(1) NOT NULL,
    marks INTEGER DEFAULT 5,
    explanation TEXT,
    question_order INTEGER DEFAULT 0
);

-- Create attempt table
CREATE TABLE attempt (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES app_user(id),
    quiz_id BIGINT NOT NULL REFERENCES quiz(id),
    score INTEGER DEFAULT 0,
    max_score INTEGER DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    time_taken_seconds INTEGER,
    answers_json TEXT
);
