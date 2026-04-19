package librarymanagementsystem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data-access layer for issue_records. Fine = Rs 10 per overdue day. */
public class IssueDAO {

    private static final BigDecimal FINE_PER_DAY = new BigDecimal("10.00");

    // ── Issue a book ─────────────────────────────────────────────
    public void issue(IssueRecord r) throws SQLException {
        // Validation: availability
        String chk = "SELECT available_qty FROM books WHERE book_id=?";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(chk)) {
            ps.setInt(1, r.getBookId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                throw new SQLException("Book not found.");
            if (rs.getInt("available_qty") < 1)
                throw new SQLException(
                    "No copies available! All copies of this book are currently issued.");
        }

        String sql = "INSERT INTO issue_records "
                   + "(book_id, student_name, student_phone, student_email, "
                   + " issue_date, due_date, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 'Issued')";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setInt   (1, r.getBookId());
            ps.setString(2, r.getStudentName());
            ps.setString(3, r.getStudentPhone());
            ps.setString(4, r.getStudentEmail());
            ps.setDate  (5, r.getIssueDate());
            ps.setDate  (6, r.getDueDate());
            ps.executeUpdate();
        }
        // Trigger in DB decrements available_qty automatically
    }

    // ── Return a book ────────────────────────────────────────────
    /** Returns the calculated fine (Rs) — may be 0 if returned on time. */
    public BigDecimal returnBook(int recordId, Date returnDate) throws SQLException {
        // Fetch due date
        String fetch = "SELECT due_date, status FROM issue_records WHERE record_id=?";
        Date dueDate;
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(fetch)) {
            ps.setInt(1, recordId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                throw new SQLException("Issue record not found.");
            if ("Returned".equals(rs.getString("status")))
                throw new SQLException("This book has already been returned.");
            dueDate = rs.getDate("due_date");
        }

        // Calculate fine
        BigDecimal fine = BigDecimal.ZERO;
        if (returnDate.after(dueDate)) {
            long days = (returnDate.getTime() - dueDate.getTime()) / (1000L * 60 * 60 * 24);
            fine = FINE_PER_DAY.multiply(new BigDecimal(days));
        }

        // Update record
        String upd = "UPDATE issue_records "
                   + "SET status='Returned', return_date=?, fine_amount=? "
                   + "WHERE record_id=?";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(upd)) {
            ps.setDate      (1, returnDate);
            ps.setBigDecimal(2, fine);
            ps.setInt       (3, recordId);
            ps.executeUpdate();
        }
        // Trigger in DB increments available_qty automatically
        return fine;
    }

    // ── Get active (issued / overdue) ────────────────────────────
    public List<IssueRecord> getActive() throws SQLException {
        // First refresh overdue status
        DatabaseConnection.get().createStatement().executeUpdate(
            "UPDATE issue_records SET status='Overdue' "
          + "WHERE status='Issued' AND due_date < CURDATE()");
        return query("ir.status IN ('Issued','Overdue') ORDER BY ir.due_date");
    }

    // ── Get all ───────────────────────────────────────────────────
    public List<IssueRecord> getAll() throws SQLException {
        return query("1=1 ORDER BY ir.created_at DESC");
    }

    // ── Search ────────────────────────────────────────────────────
    public List<IssueRecord> search(String kw) throws SQLException {
        String w = "%" + kw + "%";
        String sql = joinSql()
            + "WHERE ir.student_name LIKE ? OR b.title LIKE ? "
            + "OR ir.student_phone LIKE ? OR b.isbn LIKE ? "
            + "ORDER BY ir.created_at DESC";
        List<IssueRecord> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1,w); ps.setString(2,w);
            ps.setString(3,w); ps.setString(4,w);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Active issue count ────────────────────────────────────────
    public int activeCount() throws SQLException {
        try (Statement st = DatabaseConnection.get().createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*) FROM issue_records WHERE status IN ('Issued','Overdue')")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────
    private List<IssueRecord> query(String where) throws SQLException {
        List<IssueRecord> list = new ArrayList<>();
        try (Statement st = DatabaseConnection.get().createStatement();
             ResultSet rs = st.executeQuery(joinSql() + "WHERE " + where)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private String joinSql() {
        return "SELECT ir.*, b.title AS book_title, b.isbn "
             + "FROM issue_records ir "
             + "JOIN books b ON ir.book_id = b.book_id ";
    }

    private IssueRecord map(ResultSet rs) throws SQLException {
        IssueRecord r = new IssueRecord();
        r.setRecordId   (rs.getInt       ("record_id"));
        r.setBookId     (rs.getInt       ("book_id"));
        r.setStudentName(rs.getString    ("student_name"));
        r.setStudentPhone(rs.getString   ("student_phone"));
        r.setStudentEmail(rs.getString   ("student_email"));
        r.setIssueDate  (rs.getDate      ("issue_date"));
        r.setDueDate    (rs.getDate      ("due_date"));
        r.setReturnDate (rs.getDate      ("return_date"));
        r.setFineAmount (rs.getBigDecimal("fine_amount"));
        r.setFinePaid   (rs.getBoolean   ("fine_paid"));
        r.setStatus     (rs.getString    ("status"));
        r.setCreatedAt  (rs.getTimestamp ("created_at"));
        r.setBookTitle  (rs.getString    ("book_title"));
        r.setIsbn       (rs.getString    ("isbn"));
        return r;
    }
}
