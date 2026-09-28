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
    available_copies INTEGER NOT NULL CHECK (available_copies BETWEEN 0 AND total_copies)
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
    CHECK ((submission_date IS NULL AND status IN ('Issued', 'Late'))
        OR (submission_date IS NOT NULL AND status = 'Submitted'))
);

CREATE UNIQUE INDEX one_active_book_per_student
    ON transactions (roll_no)
    WHERE submission_date IS NULL;

-- Fine policy: Rs. 5 for each day past the due date.

INSERT INTO members (roll_no, name, email, phone, year, branch) VALUES
    ('CSE2024001', 'Aarav Sharma', 'aarav.sharma@example.com', '9000000001', 2, 'CSE'),
    ('CSE2024002', 'Diya Patel', 'diya.patel@example.com', '9000000002', 2, 'CSE'),
    ('IT2024003', 'Kabir Singh', 'kabir.singh@example.com', '9000000003', 2, 'IT'),
    ('IT2024004', 'Ananya Iyer', 'ananya.iyer@example.com', '9000000004', 2, 'IT'),
    ('AIML2024005', 'Vivaan Mehta', 'vivaan.mehta@example.com', '9000000005', 2, 'AIML'),
    ('AIML2024006', 'Ishita Rao', 'ishita.rao@example.com', '9000000006', 2, 'AIML'),
    ('EXTC2024007', 'Arjun Nair', 'arjun.nair@example.com', '9000000007', 2, 'EXTC'),
    ('EXTC2024008', 'Meera Joshi', 'meera.joshi@example.com', '9000000008', 2, 'EXTC'),
    ('ME2024009', 'Rohan Deshmukh', 'rohan.deshmukh@example.com', '9000000009', 2, 'Mechanical'),
    ('ME2024010', 'Sana Khan', 'sana.khan@example.com', '9000000010', 2, 'Mechanical'),
    ('CSE2023011', 'Aditya Kulkarni', 'aditya.kulkarni@example.com', '9000000011', 3, 'CSE'),
    ('IT2023012', 'Nisha Verma', 'nisha.verma@example.com', '9000000012', 3, 'IT'),
    ('AIML2023013', 'Reyansh Gupta', 'reyansh.gupta@example.com', '9000000013', 3, 'AIML'),
    ('EXTC2023014', 'Kavya Shah', 'kavya.shah@example.com', '9000000014', 3, 'EXTC'),
    ('ME2023015', 'Yash Malhotra', 'yash.malhotra@example.com', '9000000015', 3, 'Mechanical'),
    ('CSE2023016', 'Priya Menon', 'priya.menon@example.com', '9000000016', 3, 'CSE'),
    ('IT2023017', 'Dev Patel', 'dev.patel@example.com', '9000000017', 3, 'IT'),
    ('AIML2023018', 'Tanvi Bhosale', 'tanvi.bhosale@example.com', '9000000018', 3, 'AIML'),
    ('EXTC2023019', 'Neil Fernandes', 'neil.fernandes@example.com', '9000000019', 3, 'EXTC'),
    ('ME2023020', 'Riya Chavan', 'riya.chavan@example.com', '9000000020', 3, 'Mechanical'),
    ('CSE2022021', 'Siddharth Jain', 'siddharth.jain@example.com', '9000000021', 4, 'CSE'),
    ('IT2022022', 'Aditi Kapoor', 'aditi.kapoor@example.com', '9000000022', 4, 'IT'),
    ('AIML2022023', 'Manav Sethi', 'manav.sethi@example.com', '9000000023', 4, 'AIML'),
    ('EXTC2022024', 'Sneha Das', 'sneha.das@example.com', '9000000024', 4, 'EXTC'),
    ('ME2022025', 'Karan Bhat', 'karan.bhat@example.com', '9000000025', 4, 'Mechanical'),
    ('CSE2022026', 'Pooja Reddy', 'pooja.reddy@example.com', '9000000026', 4, 'CSE'),
    ('IT2022027', 'Rahul Bose', 'rahul.bose@example.com', '9000000027', 4, 'IT'),
    ('AIML2022028', 'Neha Agarwal', 'neha.agarwal@example.com', '9000000028', 4, 'AIML'),
    ('EXTC2022029', 'Aman Tiwari', 'aman.tiwari@example.com', '9000000029', 4, 'EXTC'),
    ('ME2022030', 'Simran Kaur', 'simran.kaur@example.com', '9000000030', 4, 'Mechanical');

INSERT INTO books (book_id, title, author, isbn, category, total_copies, available_copies) VALUES
    ('B001', 'Clean Code', 'Robert C. Martin', '9780132350884', 'Programming', 5, 3),
    ('B002', 'Effective Java', 'Joshua Bloch', '9780134685991', 'Programming', 4, 4),
    ('B003', 'Database System Concepts', 'Abraham Silberschatz', '9780078022159', 'Database', 4, 3),
    ('B004', 'Designing Data-Intensive Applications', 'Martin Kleppmann', '9781449373320', 'Database', 3, 3),
    ('B005', 'Operating System Concepts', 'Abraham Silberschatz', '9781119800361', 'Operating Systems', 3, 2),
    ('B006', 'Modern Operating Systems', 'Andrew S. Tanenbaum', '9780133591620', 'Operating Systems', 4, 4),
    ('B007', 'Computer Networks', 'Andrew S. Tanenbaum', '9780132126953', 'Networking', 3, 2),
    ('B008', 'Network Security Essentials', 'William Stallings', '9780134527338', 'Networking', 4, 4),
    ('B009', 'Introduction to Algorithms', 'Thomas H. Cormen', '9780262046305', 'DSA', 4, 3),
    ('B010', 'The Algorithm Design Manual', 'Steven S. Skiena', '9783030542551', 'DSA', 3, 3),
    ('B011', 'Artificial Intelligence: A Modern Approach', 'Stuart Russell', '9780134610993', 'Artificial Intelligence', 4, 3),
    ('B012', 'Pattern Recognition and Machine Learning', 'Christopher Bishop', '9780387310732', 'Machine Learning', 3, 3),
    ('B013', 'Hands-On Machine Learning', 'Aurelien Geron', '9781098125974', 'Machine Learning', 3, 2),
    ('B014', 'Deep Learning', 'Ian Goodfellow', '9780262035613', 'Artificial Intelligence', 3, 3),
    ('B015', 'Discrete Mathematics and Its Applications', 'Kenneth Rosen', '9781259676512', 'Mathematics', 5, 4),
    ('B016', 'Linear Algebra Done Right', 'Sheldon Axler', '9783030410253', 'Mathematics', 3, 3),
    ('B017', 'Software Engineering', 'Ian Sommerville', '9780137035151', 'Software Engineering', 4, 3),
    ('B018', 'The Pragmatic Programmer', 'David Thomas', '9780135957059', 'Software Engineering', 3, 3),
    ('B019', 'Computer Security', 'Dieter Gollmann', '9781119288077', 'Cybersecurity', 3, 2),
    ('B020', 'The Web Application Hackers Handbook', 'Dafydd Stuttard', '9781118026472', 'Cybersecurity', 3, 3),
    ('B021', 'Head First Java', 'Kathy Sierra', '9780596009205', 'Programming', 5, 4),
    ('B022', 'SQL Cookbook', 'Anthony Molinaro', '9781492077442', 'Database', 3, 3),
    ('B023', 'Linux Kernel Development', 'Robert Love', '9780672329463', 'Operating Systems', 3, 3),
    ('B024', 'Computer Networking: A Top-Down Approach', 'James Kurose', '9780136681557', 'Networking', 4, 4),
    ('B025', 'Grokking Algorithms', 'Aditya Bhargava', '9781617292231', 'DSA', 4, 4),
    ('B026', 'Python Machine Learning', 'Sebastian Raschka', '9781789955750', 'Machine Learning', 3, 3),
    ('B027', 'Probability and Statistics', 'Morris DeGroot', '9780321500465', 'Mathematics', 3, 3),
    ('B028', 'Refactoring', 'Martin Fowler', '9780134757599', 'Software Engineering', 3, 3),
    ('B029', 'Cryptography and Network Security', 'William Stallings', '9780134444284', 'Cybersecurity', 4, 4),
    ('B030', 'Data Structures Using C', 'Reema Thareja', '9780198099303', 'DSA', 4, 4);

INSERT INTO transactions (roll_no, book_id, issue_date, due_date, submission_date, status, fine) VALUES
    ('CSE2024001', 'B001', CURRENT_DATE - 4, CURRENT_DATE + 10, NULL, 'Issued', 0),
    ('CSE2024002', 'B001', CURRENT_DATE - 20, CURRENT_DATE - 6, NULL, 'Late', 30),
    ('IT2024003', 'B003', CURRENT_DATE - 3, CURRENT_DATE + 11, NULL, 'Issued', 0),
    ('IT2024004', 'B005', CURRENT_DATE - 18, CURRENT_DATE - 4, NULL, 'Late', 20),
    ('AIML2024005', 'B007', CURRENT_DATE - 5, CURRENT_DATE + 9, NULL, 'Issued', 0),
    ('AIML2024006', 'B009', CURRENT_DATE - 22, CURRENT_DATE - 8, NULL, 'Late', 40),
    ('EXTC2024007', 'B011', CURRENT_DATE - 2, CURRENT_DATE + 12, NULL, 'Issued', 0),
    ('EXTC2024008', 'B013', CURRENT_DATE - 16, CURRENT_DATE - 2, NULL, 'Late', 10),
    ('ME2024009', 'B015', CURRENT_DATE - 6, CURRENT_DATE + 8, NULL, 'Issued', 0),
    ('ME2024010', 'B017', CURRENT_DATE - 19, CURRENT_DATE - 5, NULL, 'Late', 25),
    ('CSE2023011', 'B019', CURRENT_DATE - 7, CURRENT_DATE + 7, NULL, 'Issued', 0),
    ('IT2023012', 'B021', CURRENT_DATE - 17, CURRENT_DATE - 3, NULL, 'Late', 15),
    ('AIML2023013', 'B002', CURRENT_DATE - 25, CURRENT_DATE - 11, CURRENT_DATE - 12, 'Submitted', 0),
    ('EXTC2023014', 'B004', CURRENT_DATE - 30, CURRENT_DATE - 16, CURRENT_DATE - 10, 'Submitted', 30),
    ('ME2023015', 'B006', CURRENT_DATE - 24, CURRENT_DATE - 10, CURRENT_DATE - 10, 'Submitted', 0),
    ('CSE2023016', 'B008', CURRENT_DATE - 28, CURRENT_DATE - 14, CURRENT_DATE - 9, 'Submitted', 25),
    ('IT2023017', 'B010', CURRENT_DATE - 22, CURRENT_DATE - 8, CURRENT_DATE - 8, 'Submitted', 0),
    ('AIML2023018', 'B012', CURRENT_DATE - 35, CURRENT_DATE - 21, CURRENT_DATE - 16, 'Submitted', 25),
    ('EXTC2023019', 'B014', CURRENT_DATE - 26, CURRENT_DATE - 12, CURRENT_DATE - 12, 'Submitted', 0),
    ('ME2023020', 'B016', CURRENT_DATE - 29, CURRENT_DATE - 15, CURRENT_DATE - 11, 'Submitted', 20),
    ('CSE2022021', 'B018', CURRENT_DATE - 23, CURRENT_DATE - 9, CURRENT_DATE - 9, 'Submitted', 0),
    ('IT2022022', 'B020', CURRENT_DATE - 31, CURRENT_DATE - 17, CURRENT_DATE - 12, 'Submitted', 25),
    ('AIML2022023', 'B022', CURRENT_DATE - 27, CURRENT_DATE - 13, CURRENT_DATE - 13, 'Submitted', 0),
    ('EXTC2022024', 'B023', CURRENT_DATE - 32, CURRENT_DATE - 18, CURRENT_DATE - 14, 'Submitted', 20),
    ('ME2022025', 'B024', CURRENT_DATE - 21, CURRENT_DATE - 7, CURRENT_DATE - 7, 'Submitted', 0),
    ('CSE2022026', 'B025', CURRENT_DATE - 36, CURRENT_DATE - 22, CURRENT_DATE - 15, 'Submitted', 35),
    ('IT2022027', 'B026', CURRENT_DATE - 20, CURRENT_DATE - 6, CURRENT_DATE - 6, 'Submitted', 0),
    ('AIML2022028', 'B027', CURRENT_DATE - 33, CURRENT_DATE - 19, CURRENT_DATE - 13, 'Submitted', 30),
    ('EXTC2022029', 'B028', CURRENT_DATE - 18, CURRENT_DATE - 4, CURRENT_DATE - 4, 'Submitted', 0),
    ('ME2022030', 'B029', CURRENT_DATE - 34, CURRENT_DATE - 20, CURRENT_DATE - 14, 'Submitted', 30);
