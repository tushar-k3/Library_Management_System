package librarymanagementsystem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static librarymanagementsystem.Theme.*;

/**
 * Issue panel — completely redesigned form layout.
 *
 * Form is a FIXED top section (not in a split pane top) so it is
 * NEVER clipped or scrolled out of view. All required fields are
 * always visible: Name, Phone, Email, Book, Issue Date, Due Date.
 *
 * Validation fires BEFORE anything is sent to the DB:
 *   1. Student Name  — required
 *   2. Phone Number  — required
 *   3. Email Address — required
 *   4. Book selected — required
 *   5. Book has available copies
 *   6. Due date is after issue date
 */
public class IssuePanel extends JPanel {

    private final BookDAO  bookDAO  = new BookDAO();
    private final IssueDAO issueDAO = new IssueDAO();

    // ── Form fields (class-level — never null after build()) ──────
    private JTextField      nameF;
    private JTextField      phoneF;
    private JTextField      emailF;
    private JTextField      issueDateF;
    private JTextField      dueDateF;
    private JComboBox<Book> bookCombo;
    private JLabel          availLabel;

    // ── Table fields ──────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchF;

    private static final String[] COLS = {
        "#", "Book Title", "Student Name", "Phone", "Email",
        "Issue Date", "Due Date", "Status"
    };

    IssuePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_APP_BG);

        // Initialise all fields BEFORE build() so they are never null
        nameF      = field(24);
        phoneF     = field(24);
        emailF     = field(24);
        issueDateF = field(24);
        dueDateF   = field(24);
        bookCombo  = new JComboBox<>();
        availLabel = new JLabel("  —  ");

        issueDateF.setText(LocalDate.now().toString());
        issueDateF.setEditable(false);
        issueDateF.setBackground(new Color(245, 247, 250));
        issueDateF.setForeground(C_TXT_MUTED);
        dueDateF.setText(LocalDate.now().plusDays(14).toString());
        bookCombo.setFont(F_BODY);
        bookCombo.setBackground(C_WHITE);
        bookCombo.addActionListener(e -> updateAvailLabel());
        availLabel.setFont(F_SECTION);

        build();
        reloadBooks();
        loadIssues();
    }

    // ─────────────────────────────────────────────────────────────
    //  MAIN LAYOUT
    //  NORTH  = topBar (page title)
    //  CENTER = form card  (fixed, never clips)
    //  SOUTH  = table panel (fills remaining space)
    // ─────────────────────────────────────────────────────────────
    private void build() {
        add(topBar("Issue Book",
            "Fill all student details, choose a book and click  + Issue Book  |  Fine: Rs 10 / day overdue"),
            BorderLayout.NORTH);

        // Outer wrapper gives the form its own padding
        JPanel centerWrap = new JPanel(new BorderLayout(0, 10));
        centerWrap.setBackground(C_APP_BG);
        centerWrap.setBorder(new EmptyBorder(14, 22, 0, 22));
        centerWrap.add(buildForm(),  BorderLayout.NORTH);   // form — fixed height
        centerWrap.add(buildTable(), BorderLayout.CENTER);  // table — expands
        add(centerWrap, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────
    //  FORM CARD  — all fields in a simple 2-column grid,
    //               no split pane, always fully visible
    // ─────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(C_WHITE);
        card.setBorder(new LineBorder(C_BORDER, 1, true));

        // ── Card header ───────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
            new EmptyBorder(10, 6, 10, 6)));

        JPanel greenBar = new JPanel();
        greenBar.setBackground(C_GREEN);
        greenBar.setPreferredSize(new Dimension(4, 22));
        header.add(greenBar);

        JLabel title = new JLabel("Issue a Book to Student  —  All fields marked * are required");
        title.setFont(F_SECTION);
        title.setForeground(C_SIDEBAR);
        header.add(title);

        card.add(header, BorderLayout.NORTH);

        // ── Fields grid ───────────────────────────────────────────
        // Layout: Label | Field | Label | Field (4 columns)
        // Row 0: Student Name *        | Select Book *
        // Row 1: Phone Number *        | Availability
        // Row 2: Email Address *       | Issue Date (auto)
        // Row 3: Due Date *            | hint text
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(C_WHITE);
        grid.setBorder(new EmptyBorder(14, 20, 10, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Column widths: label | field | gap | label | field
        // Col 0 = label (fixed), Col 1 = field (grows),
        // Col 2 = spacer,        Col 3 = label (fixed), Col 4 = field (grows)
        g.insets = new Insets(6, 4, 6, 8);

        // ── Row 0 ─────────────────────────────────────────────────
        addLbl(grid, g, 0, 0, "Student Name *");
        addFld(grid, g, 0, 1, nameF);
        addSep(grid, g, 0, 2);
        addLbl(grid, g, 0, 3, "Select Book *");
        addFld(grid, g, 0, 4, bookCombo);

        // ── Row 1 ─────────────────────────────────────────────────
        addLbl(grid, g, 1, 0, "Phone Number *");
        addFld(grid, g, 1, 1, phoneF);
        addSep(grid, g, 1, 2);
        addLbl(grid, g, 1, 3, "Availability:");
        g.gridx = 4; g.gridy = 1; g.weightx = 1;
        grid.add(availLabel, g);

        // ── Row 2 ─────────────────────────────────────────────────
        addLbl(grid, g, 2, 0, "Email Address *");
        addFld(grid, g, 2, 1, emailF);
        addSep(grid, g, 2, 2);
        addLbl(grid, g, 2, 3, "Issue Date (auto)");
        addFld(grid, g, 2, 4, issueDateF);

        // ── Row 3 ─────────────────────────────────────────────────
        addLbl(grid, g, 3, 0, "Due Date *");
        addFld(grid, g, 3, 1, dueDateF);
        addSep(grid, g, 3, 2);
        g.gridx = 3; g.gridy = 3; g.weightx = 0; g.gridwidth = 2;
        grid.add(muted("Format: YYYY-MM-DD  |  Fine = Rs 10 per day after due date"), g);
        g.gridwidth = 1;  // reset

        card.add(grid, BorderLayout.CENTER);

        // ── Button row ────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(new Color(248, 250, 252));
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

        JButton reloadBtn = btnOutline("Reload Books");
        JButton clearBtn  = btnOutline("Clear Form");
        JButton issueBtn  = btnSuccess("+ Issue Book");

        reloadBtn.addActionListener(e -> reloadBooks());
        clearBtn .addActionListener(e -> clearForm());
        issueBtn .addActionListener(e -> issueBook());

        btnRow.add(reloadBtn);
        btnRow.add(clearBtn);
        btnRow.add(issueBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    // ── GridBag helpers ───────────────────────────────────────────
    private void addLbl(JPanel p, GridBagConstraints g, int row, int col, String text) {
        g.gridx = col; g.gridy = row; g.weightx = 0; g.gridwidth = 1;
        p.add(lbl(text), g);
    }
    private void addFld(JPanel p, GridBagConstraints g, int row, int col, Component comp) {
        g.gridx = col; g.gridy = row; g.weightx = 1; g.gridwidth = 1;
        p.add(comp, g);
    }
    private void addSep(JPanel p, GridBagConstraints g, int row, int col) {
        // invisible gap column between left and right halves
        g.gridx = col; g.gridy = row; g.weightx = 0; g.gridwidth = 1;
        JPanel gap = new JPanel();
        gap.setBackground(C_WHITE);
        gap.setPreferredSize(new Dimension(22, 1));
        p.add(gap, g);
    }

    // ─────────────────────────────────────────────────────────────
    //  TABLE
    // ─────────────────────────────────────────────────────────────
    private JPanel buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_APP_BG);

        JPanel tb = new JPanel(new BorderLayout(10, 0));
        tb.setBackground(C_APP_BG);
        tb.setBorder(new EmptyBorder(10, 0, 8, 0));

        JLabel heading = new JLabel("Active & Overdue Issues");
        heading.setFont(F_SECTION);
        heading.setForeground(C_TXT_DARK);
        tb.add(heading, BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        ctrl.setBackground(C_APP_BG);
        searchF = field(18);
        JButton sb   = btnPrimary("Search");
        JButton allB = btnOutline("All Issues");
        JButton refB = btnOutline("Refresh");
        sb  .addActionListener(e -> searchIssues());
        allB.addActionListener(e -> loadAll());
        refB.addActionListener(e -> loadIssues());
        ctrl.add(searchF); ctrl.add(sb); ctrl.add(allB); ctrl.add(refB);
        tb.add(ctrl, BorderLayout.EAST);
        panel.add(tb, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        int[] w = {55, 200, 150, 105, 160, 100, 100, 80};
        for (int i = 0; i < w.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        panel.add(scroll(table), BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────
    //  AVAILABILITY LABEL
    // ─────────────────────────────────────────────────────────────
    private void updateAvailLabel() {
        Book sel = (Book) bookCombo.getSelectedItem();
        if (sel == null) {
            availLabel.setText("  —  ");
            availLabel.setForeground(C_TXT_MUTED);
            return;
        }
        int av  = sel.getAvailableQty();
        int tot = sel.getTotalQty();
        if (av == 0) {
            availLabel.setText("  0 / " + tot + "   NOT AVAILABLE  ");
            availLabel.setForeground(C_RED);
        } else if (av == 1) {
            availLabel.setText("  1 / " + tot + "   Last copy!  ");
            availLabel.setForeground(C_AMBER);
        } else {
            availLabel.setText("  " + av + " / " + tot + "   Available  ");
            availLabel.setForeground(C_GREEN);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  ISSUE ACTION — validation runs FIRST, nothing reaches DB
    //  unless every check passes
    // ─────────────────────────────────────────────────────────────
    private void issueBook() {

        // ── Step 1: collect and validate every required field ─────
        String name  = nameF.getText().trim();
        String phone = phoneF.getText().trim();
        String email = emailF.getText().trim();

        if (name.isEmpty()) {
            highlightError(nameF);
            warn("Student Name is required.\nPlease enter the student's full name.");
            return;
        }
        if (phone.isEmpty()) {
            highlightError(phoneF);
            warn("Phone Number is required.\nPlease enter the student's phone number.");
            return;
        }
        if (email.isEmpty()) {
            highlightError(emailF);
            warn("Email Address is required.\nPlease enter the student's email address.");
            return;
        }

        // ── Step 2: book selection ────────────────────────────────
        Book sel = (Book) bookCombo.getSelectedItem();
        if (sel == null) {
            warn("Please select a book from the dropdown.");
            return;
        }

        // ── Step 3: availability (client-side) ────────────────────
        if (sel.getAvailableQty() < 1) {
            JOptionPane.showMessageDialog(this,
                "\"" + sel.getTitle() + "\" has NO available copies.\n"
                + "All " + sel.getTotalQty() + " copies are currently issued.\n"
                + "Please choose a different book or wait for a return.",
                "Book Not Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Step 4: date validation ───────────────────────────────
        Date issueDate, dueDate;
        try {
            issueDate = Date.valueOf(issueDateF.getText().trim());
            dueDate   = Date.valueOf(dueDateF.getText().trim());
        } catch (IllegalArgumentException e) {
            highlightError(dueDateF);
            warn("Due Date must be in YYYY-MM-DD format (e.g. 2026-06-15).");
            return;
        }
        if (!dueDate.after(issueDate)) {
            highlightError(dueDateF);
            warn("Due Date must be a date AFTER the Issue Date (" + issueDate + ").");
            return;
        }

        // ── Step 5: build record and call DAO ────────────────────
        IssueRecord rec = new IssueRecord();
        rec.setBookId      (sel.getBookId());
        rec.setStudentName (name);
        rec.setStudentPhone(phone);
        rec.setStudentEmail(email);
        rec.setIssueDate   (issueDate);
        rec.setDueDate     (dueDate);

        try {
            issueDAO.issue(rec);   // DAO also re-checks availability server-side
            JOptionPane.showMessageDialog(this,
                "Book issued successfully!\n\n"
                + "Book    : " + sel.getTitle()     + "\n"
                + "Student : " + name               + "\n"
                + "Phone   : " + phone              + "\n"
                + "Email   : " + email              + "\n"
                + "Due By  : " + dueDate            + "\n\n"
                + "Fine = Rs 10/day if returned after due date.",
                "Issued Successfully", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            reloadBooks();
            loadIssues();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Issue failed:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Flash a red border on a field so the user sees which one failed. */
    private void highlightError(JTextField tf) {
        tf.requestFocus();
        tf.selectAll();
        Color orig = tf.getBorder() != null
            ? ((CompoundBorder) tf.getBorder()).getOutsideBorder() == null
                ? C_BORDER : C_BORDER
            : C_BORDER;
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_RED, 2, true),
            new EmptyBorder(6, 9, 6, 9)));
        // Restore border after 1.5 s
        Timer t = new Timer(1500, e -> tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true), new EmptyBorder(7, 10, 7, 10))));
        t.setRepeats(false);
        t.start();
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private void clearForm() {
        nameF.setText("");
        phoneF.setText("");
        emailF.setText("");
        issueDateF.setText(LocalDate.now().toString());
        dueDateF.setText(LocalDate.now().plusDays(14).toString());
        if (bookCombo.getItemCount() > 0) bookCombo.setSelectedIndex(0);
        updateAvailLabel();
    }

    void reloadBooks() {
        try {
            bookCombo.removeAllItems();
            for (Book b : bookDAO.getAll()) bookCombo.addItem(b);
            updateAvailLabel();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadIssues() {
        try { fill(issueDAO.getActive()); }
        catch (SQLException ex) { err(ex.getMessage()); }
    }
    private void loadAll() {
        try { fill(issueDAO.getAll()); }
        catch (SQLException ex) { err(ex.getMessage()); }
    }
    private void searchIssues() {
        try { fill(issueDAO.search(searchF.getText().trim())); }
        catch (SQLException ex) { err(ex.getMessage()); }
    }

    private void fill(List<IssueRecord> list) {
        tableModel.setRowCount(0);
        for (IssueRecord r : list) {
            tableModel.addRow(new Object[]{
                r.getRecordId(),
                r.getBookTitle(),
                r.getStudentName(),
                r.getStudentPhone(),
                r.getStudentEmail(),
                r.getIssueDate(),
                r.getDueDate(),
                r.getStatus()
            });
        }
    }

    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Required Field Missing",
            JOptionPane.WARNING_MESSAGE);
    }
    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
