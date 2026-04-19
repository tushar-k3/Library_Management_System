package librarymanagementsystem;

import java.sql.Timestamp;

/** Represents one book title with quantity tracking. */
public class Book {

    private int       bookId;
    private String    isbn;
    private String    title;
    private String    author;
    private String    genre;
    private int       totalQty;
    private int       availableQty;
    private Timestamp dateAdded;

    public Book() {}

    public Book(String isbn, String title, String author, String genre, int totalQty) {
        this.isbn         = isbn;
        this.title        = title;
        this.author       = author;
        this.genre        = genre;
        this.totalQty     = totalQty;
        this.availableQty = totalQty;
    }

    // ── getters ──────────────────────────────────────────────────
    public int       getBookId()      { return bookId; }
    public String    getIsbn()        { return isbn; }
    public String    getTitle()       { return title; }
    public String    getAuthor()      { return author; }
    public String    getGenre()       { return genre; }
    public int       getTotalQty()    { return totalQty; }
    public int       getAvailableQty(){ return availableQty; }
    public int       getIssuedQty()   { return totalQty - availableQty; }
    public Timestamp getDateAdded()   { return dateAdded; }

    // ── setters ──────────────────────────────────────────────────
    public void setBookId(int v)          { bookId = v; }
    public void setIsbn(String v)         { isbn = v; }
    public void setTitle(String v)        { title = v; }
    public void setAuthor(String v)       { author = v; }
    public void setGenre(String v)        { genre = v; }
    public void setTotalQty(int v)        { totalQty = v; }
    public void setAvailableQty(int v)    { availableQty = v; }
    public void setDateAdded(Timestamp v) { dateAdded = v; }

    @Override
    public String toString() {
        return title + "  [" + availableQty + " avail]";
    }
}
