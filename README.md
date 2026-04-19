# LibraSystem — Library Management System
### Single Package: `librarymanagementsystem` | Java Swing + MySQL

---

## File Structure

```
LibraryManagementSystem/
├── database_setup.sql              ← Run this FIRST in MySQL
└── src/
    └── librarymanagementsystem/    ← ALL files in one package
        ├── Main.java               ← Entry point (Run As > Java Application)
        ├── DatabaseConnection.java ← Edit DB credentials here
        ├── Book.java               ← Book model
        ├── IssueRecord.java        ← Issue record model
        ├── BookDAO.java            ← Book database operations
        ├── IssueDAO.java           ← Issue/Return database operations
        ├── Theme.java              ← Light design system, colours, fonts
        ├── MainFrame.java          ← App window + sidebar navigation
        ├── DashboardPanel.java     ← Stats dashboard
        ├── BooksPanel.java         ← Book catalogue management
        ├── IssuePanel.java         ← Issue book to student
        └── ReturnPanel.java        ← Process book returns + fine calc
```

---

## Setup in 3 Steps

### Step 1 — MySQL
Open MySQL Workbench, connect to localhost, then run `database_setup.sql`.
This creates the `db` database, tables, triggers, and 5 sample books.

### Step 2 — Credentials
Open `DatabaseConnection.java` and update:
```java
private static final String USER     = "root";  // your MySQL username
private static final String PASSWORD = "root";  // your MySQL password
```

### Step 3 — Eclipse
1. **File > New > Java Project** — name it `LibraryManagementSystem`
2. Copy the `src/librarymanagementsystem/` folder into your project's `src/`
3. Right-click project → **Build Path > Configure Build Path**
4. **Libraries tab > Add External JARs** → select `mysql-connector-j-*.jar`
   (Download from https://dev.mysql.com/downloads/connector/j/)
5. Click **Apply and Close**
6. Open `Main.java` → Right-click → **Run As > Java Application**

---

## Features

### Dashboard
- Total Book Titles, Total Copies, Available Copies, Issued Copies, Active Issues
- 3-step guide on how to use the app

### Books (BooksPanel)
| Field | Notes |
|---|---|
| ISBN | Unique, required |
| Title | Required |
| Author | Required |
| Genre | Optional |
| Total Copies | Required, min 1 |
| Available Copies | Auto-managed by MySQL triggers |
| Issued Copies | = Total - Available (calculated) |

- Search by title / author / ISBN / genre
- Edit (double-click or button)
- Delete (blocked if copies are issued)

### Issue Book (IssuePanel)
| Field | Notes |
|---|---|
| Student Name | Required |
| Phone | Optional |
| Email | Optional |
| Book | Dropdown — shows title + available count |
| Issue Date | Auto-filled with today's date (read-only) |
| Due Date | Editable, default = today + 14 days |

**Validations:**
- Student name must not be empty
- Book must have at least 1 available copy (checked on client AND server)
- Due date must be after issue date
- MySQL trigger auto-decrements `available_qty`

### Return Book (ReturnPanel)
- Shows all active (Issued / Overdue) records
- Overdue rows highlighted in red
- "Days left / overdue" column calculated live
- Return date defaults to today (editable)
- **Fine = Rs 10 per day overdue** (auto-calculated)
- Confirmation dialog shows fine before processing
- MySQL trigger auto-increments `available_qty`

---

## Database Schema

### `books`
```sql
book_id, isbn (UNIQUE), title, author, genre,
total_qty, available_qty, date_added
```

### `issue_records`
```sql
record_id, book_id (FK), student_name, student_phone, student_email,
issue_date, due_date, return_date, fine_amount, fine_paid,
status (Issued|Returned|Overdue), created_at
```

### Triggers
- `trg_after_issue` — on INSERT, decrements `available_qty`
- `trg_after_return` — on UPDATE to Returned, increments `available_qty`

---

## Dependencies
| Dependency | Version | How |
|---|---|---|
| JDK | 11 or higher | Already installed |
| mysql-connector-j | 8.x+ | Add .jar to build path |
| Java Swing | Built-in | No extra install |
