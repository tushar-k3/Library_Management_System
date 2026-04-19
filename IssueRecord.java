package librarymanagementsystem;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/** Represents one book-issue transaction. */
public class IssueRecord {

    private int        recordId;
    private int        bookId;
    private String     studentName;
    private String     studentPhone;
    private String     studentEmail;
    private Date       issueDate;
    private Date       dueDate;
    private Date       returnDate;
    private BigDecimal fineAmount;
    private boolean    finePaid;
    private String     status;       // Issued | Returned | Overdue
    private Timestamp  createdAt;

    // display-only (joined from books)
    private String bookTitle;
    private String isbn;

    public IssueRecord() {}

    // ── getters ──────────────────────────────────────────────────
    public int        getRecordId()    { return recordId; }
    public int        getBookId()      { return bookId; }
    public String     getStudentName() { return studentName; }
    public String     getStudentPhone(){ return studentPhone; }
    public String     getStudentEmail(){ return studentEmail; }
    public Date       getIssueDate()   { return issueDate; }
    public Date       getDueDate()     { return dueDate; }
    public Date       getReturnDate()  { return returnDate; }
    public BigDecimal getFineAmount()  { return fineAmount; }
    public boolean    isFinePaid()     { return finePaid; }
    public String     getStatus()      { return status; }
    public Timestamp  getCreatedAt()   { return createdAt; }
    public String     getBookTitle()   { return bookTitle; }
    public String     getIsbn()        { return isbn; }

    // ── setters ──────────────────────────────────────────────────
    public void setRecordId(int v)           { recordId = v; }
    public void setBookId(int v)             { bookId = v; }
    public void setStudentName(String v)     { studentName = v; }
    public void setStudentPhone(String v)    { studentPhone = v; }
    public void setStudentEmail(String v)    { studentEmail = v; }
    public void setIssueDate(Date v)         { issueDate = v; }
    public void setDueDate(Date v)           { dueDate = v; }
    public void setReturnDate(Date v)        { returnDate = v; }
    public void setFineAmount(BigDecimal v)  { fineAmount = v; }
    public void setFinePaid(boolean v)       { finePaid = v; }
    public void setStatus(String v)          { status = v; }
    public void setCreatedAt(Timestamp v)    { createdAt = v; }
    public void setBookTitle(String v)       { bookTitle = v; }
    public void setIsbn(String v)            { isbn = v; }
}
