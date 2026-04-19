package librarymanagementsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data-access layer for the books table. */
public class BookDAO {

    // ── Add ──────────────────────────────────────────────────────
    public void addBook(Book b) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, genre, total_qty, available_qty) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getGenre());
            ps.setInt   (5, b.getTotalQty());
            ps.setInt   (6, b.getTotalQty());
            ps.executeUpdate();
        }
    }

    // ── Update ───────────────────────────────────────────────────
    public void updateBook(Book b) throws SQLException {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, genre=?, "
                   + "total_qty=?, available_qty=? WHERE book_id=?";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getGenre());
            ps.setInt   (5, b.getTotalQty());
            ps.setInt   (6, b.getAvailableQty());
            ps.setInt   (7, b.getBookId());
            ps.executeUpdate();
        }
    }

    // ── Delete ───────────────────────────────────────────────────
    public void deleteBook(int bookId) throws SQLException {
        // Block if copies are currently issued
        String chk = "SELECT (total_qty - available_qty) AS issued FROM books WHERE book_id=?";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(chk)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("issued") > 0)
                throw new SQLException("Cannot delete: book has issued copies.");
        }
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(
                "DELETE FROM books WHERE book_id=?")) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
    }

    // ── Fetch all ─────────────────────────────────────────────────
    public List<Book> getAll() throws SQLException {
        List<Book> list = new ArrayList<>();
        try (Statement st = DatabaseConnection.get().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM books ORDER BY title")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Fetch by id ───────────────────────────────────────────────
    public Book getById(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(
                "SELECT * FROM books WHERE book_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        }
        return null;
    }

    // ── Search ────────────────────────────────────────────────────
    public List<Book> search(String kw) throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? "
                   + "OR isbn LIKE ? OR genre LIKE ? ORDER BY title";
        String w = "%" + kw + "%";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1,w); ps.setString(2,w); ps.setString(3,w); ps.setString(4,w);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── Stats ─────────────────────────────────────────────────────
    /** Returns int[]{totalTitles, totalCopies, totalAvailable, totalIssued} */
    public int[] stats() throws SQLException {
        try (Statement st = DatabaseConnection.get().createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*), SUM(total_qty), SUM(available_qty), "
               + "SUM(total_qty - available_qty) FROM books")) {
            if (rs.next())
                return new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)};
        }
        return new int[]{0, 0, 0, 0};
    }

    // ── Row mapper ────────────────────────────────────────────────
    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId      (rs.getInt      ("book_id"));
        b.setIsbn        (rs.getString   ("isbn"));
        b.setTitle       (rs.getString   ("title"));
        b.setAuthor      (rs.getString   ("author"));
        b.setGenre       (rs.getString   ("genre"));
        b.setTotalQty    (rs.getInt      ("total_qty"));
        b.setAvailableQty(rs.getInt      ("available_qty"));
        b.setDateAdded   (rs.getTimestamp("date_added"));
        return b;
    }
}
