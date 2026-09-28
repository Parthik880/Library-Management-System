-- Run this file after creating/selecting the library_db PostgreSQL database.

CREATE TABLE members (
    roll_no VARCHAR(30) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(30),
    year INTEGER NOT NULL CHECK (year BETWEEN 1 AND 10),
    branch VARCHAR(100) NOT NULL
);

CREATE TABLE books (
    book_id VARCHAR(30) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(100) NOT NULL,
    total_copies INTEGER NOT NULL CHECK (total_copies >= 0),
    available_copies INTEGER NOT NULL CHECK (
        available_copies BETWEEN 0 AND total_copies
    )
);

CREATE TABLE transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    roll_no VARCHAR(30) NOT NULL REFERENCES members(roll_no),
    book_id VARCHAR(30) NOT NULL REFERENCES books(book_id),
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    submission_date DATE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Issued', 'Late', 'Submitted')),
    fine NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (fine >= 0),
    CHECK (due_date >= issue_date),
    CHECK (
        (submission_date IS NULL AND status IN ('Issued', 'Late'))
        OR (submission_date IS NOT NULL AND status = 'Submitted')
    )
);

-- A member may have one current issue; returned rows remain as history.
CREATE UNIQUE INDEX one_active_book_per_student
    ON transactions (roll_no)
    WHERE submission_date IS NULL;

-- Fine policy: Rs. 5 for each day past due_date.

INSERT INTO members (roll_no, name, email, phone, year, branch) VALUES
    ('2024CS001', 'Aarav Sharma', 'aarav.sharma@example.com', '9876543210', 2, 'Computer Science'),
    ('2024EC002', 'Diya Patel', 'diya.patel@example.com', '9876543211', 2, 'Electronics'),
    ('2023ME003', 'Kabir Singh', 'kabir.singh@example.com', '9876543212', 3, 'Mechanical');

INSERT INTO books (book_id, title, author, isbn, category, total_copies, available_copies) VALUES
    ('BK001', 'Clean Code', 'Robert C. Martin', '9780132350884', 'Programming', 3, 2),
    ('BK002', 'Database System Concepts', 'Abraham Silberschatz', '9780078022159', 'Database', 2, 2),
    ('BK003', 'Introduction to Algorithms', 'Thomas H. Cormen', '9780262046305', 'Algorithms', 1, 0);

INSERT INTO transactions (roll_no, book_id, issue_date, due_date, submission_date, status, fine) VALUES
    ('2024CS001', 'BK001', CURRENT_DATE - 3, CURRENT_DATE + 11, NULL, 'Issued', 0),
    ('2024EC002', 'BK002', CURRENT_DATE - 21, CURRENT_DATE - 7, CURRENT_DATE - 2, 'Submitted', 25),
    ('2023ME003', 'BK003', CURRENT_DATE - 20, CURRENT_DATE - 6, NULL, 'Late', 30);
