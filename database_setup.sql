
CREATE DATABASE IF NOT EXISTS db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db;

-- ── books ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS books (
    book_id        INT          AUTO_INCREMENT PRIMARY KEY,
    isbn           VARCHAR(25)  UNIQUE NOT NULL,
    title          VARCHAR(255) NOT NULL,
    author         VARCHAR(255) NOT NULL,
    genre          VARCHAR(100),
    total_qty      INT          NOT NULL DEFAULT 1,
    available_qty  INT          NOT NULL DEFAULT 1,
    date_added     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_total  CHECK (total_qty     >= 0),
    CONSTRAINT chk_avail  CHECK (available_qty >= 0)
);

-- ── issue_records ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS issue_records (
    record_id      INT           AUTO_INCREMENT PRIMARY KEY,
    book_id        INT           NOT NULL,
    student_name   VARCHAR(255)  NOT NULL,
    student_phone  VARCHAR(20),
    student_email  VARCHAR(255),
    issue_date     DATE          NOT NULL,
    due_date       DATE          NOT NULL,
    return_date    DATE,
    fine_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fine_paid      BOOLEAN       NOT NULL DEFAULT FALSE,
    status         ENUM('Issued','Returned','Overdue') NOT NULL DEFAULT 'Issued',
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT
);

-- ── triggers ─────────────────────────────────────────────
DROP TRIGGER IF EXISTS trg_after_issue;
DROP TRIGGER IF EXISTS trg_after_return;

DELIMITER $$

CREATE TRIGGER trg_after_issue
AFTER INSERT ON issue_records
FOR EACH ROW
BEGIN
    IF NEW.status = 'Issued' THEN
        UPDATE books SET available_qty = available_qty - 1 WHERE book_id = NEW.book_id;
    END IF;
END$$

CREATE TRIGGER trg_after_return
AFTER UPDATE ON issue_records
FOR EACH ROW
BEGIN
    IF OLD.status <> 'Returned' AND NEW.status = 'Returned' THEN
        UPDATE books SET available_qty = available_qty + 1 WHERE book_id = NEW.book_id;
    END IF;
END$$

DELIMITER ;
