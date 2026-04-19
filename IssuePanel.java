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

public class IssuePanel extends JPanel {

    private final BookDAO  bookDAO  = new BookDAO();
    private final IssueDAO issueDAO = new IssueDAO();

    private JComboBox<Book> bookCombo;
    private JLabel availLabel;
    private JTextField nameF, phoneF, emailF, issueDateF, dueDateF;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchF;

    private static final String[] COLS = {
        "#", "Book Title", "Student Name", "Phone", "Email",
        "Issue Date", "Due Date", "Status"
    };

    IssuePanel() {
        setLayout(new BorderLayout());
        setBackground(C_APP_BG);
        build();
        reloadBooks();
        loadIssues();
    }

    private void build() {
        add(topBar("Issue Book",
            "Fill student details, choose a book and click Issue  |  Fine: Rs 10/day overdue"),
            BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(300);
        split.setDividerSize(6);
        split.setBorder(null);

        split.setTopComponent(buildForm());
        split.setBottomComponent(buildTable());

        add(split, BorderLayout.CENTER);
    }

    // ✅ FIXED FORM
    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_APP_BG);
        outer.setBorder(new EmptyBorder(14, 22, 6, 22));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_WHITE);
        card.setBorder(new LineBorder(C_BORDER, 1, true));

        JPanel ch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ch.setBackground(new Color(248, 250, 252));

        JPanel accent = new JPanel();
        accent.setBackground(C_GREEN);
        accent.setPreferredSize(new Dimension(4, 38));
        ch.add(accent);

        JLabel title = new JLabel("Issue a Book to Student");
        title.setFont(F_SECTION);
        ch.add(title);

        card.add(ch, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(C_WHITE);
        fields.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        // fields
        nameF = field(20);
        phoneF = field(20);
        emailF = field(20);
        issueDateF = field(20);
        dueDateF = field(20);

        issueDateF.setText(LocalDate.now().toString());
        issueDateF.setEditable(false);

        dueDateF.setText(LocalDate.now().plusDays(14).toString());

        bookCombo = new JComboBox<>();
        bookCombo.setPreferredSize(new Dimension(280, 32));
        bookCombo.addActionListener(e -> updateAvailLabel());

        availLabel = new JLabel("—");
        availLabel.setFont(F_SECTION);

        // layout rows
        row(fields, g, 0, 0, "Student Name *", nameF);
        row(fields, g, 0, 2, "Select Book *", bookCombo);

        row(fields, g, 1, 0, "Phone Number", phoneF);

        g.gridx = 2; g.gridy = 1;
        fields.add(lbl("Availability:"), g);
        g.gridx = 3;
        fields.add(availLabel, g);

        row(fields, g, 2, 0, "Email Address", emailF);
        row(fields, g, 2, 2, "Issue Date (auto)", issueDateF);

        g.gridx = 2; g.gridy = 3;
        fields.add(lbl("Due Date *"), g);
        g.gridx = 3;
        fields.add(dueDateF, g);

        card.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(new Color(248, 250, 252));

        JButton reloadBtn = btnOutline("Reload Books");
        JButton clearBtn = btnOutline("Clear Form");
        JButton issueBtn = btnSuccess("+ Issue Book");

        reloadBtn.addActionListener(e -> reloadBooks());
        clearBtn.addActionListener(e -> clearForm());
        issueBtn.addActionListener(e -> issueBook());

        btnRow.add(reloadBtn);
        btnRow.add(clearBtn);
        btnRow.add(issueBtn);

        card.add(btnRow, BorderLayout.SOUTH);
        outer.add(card);
        return outer;
    }

    private void row(JPanel p, GridBagConstraints g,
                     int row, int col, String label, Component comp) {

        g.gridx = col;
        g.gridy = row;
        g.weightx = 0;
        p.add(lbl(label), g);

        g.gridx = col + 1;
        g.weightx = 1;
        p.add(comp, g);
    }

    // table
    private JPanel buildTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_APP_BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(C_APP_BG);
        top.setBorder(new EmptyBorder(10, 22, 10, 22));

        JLabel title = new JLabel("Active & Overdue Issues");
        title.setFont(F_SECTION);
        top.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(C_APP_BG);

        searchF = field(18);
        JButton search = btnPrimary("Search");
        JButton all = btnOutline("All Issues");
        JButton refresh = btnOutline("Refresh");

        search.addActionListener(e -> searchIssues());
        all.addActionListener(e -> loadAll());
        refresh.addActionListener(e -> loadIssues());

        right.add(searchF);
        right.add(search);
        right.add(all);
        right.add(refresh);

        top.add(right, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        panel.add(scroll(table), BorderLayout.CENTER);
        return panel;
    }

    // logic (unchanged)
    private void updateAvailLabel() {
        Book b = (Book) bookCombo.getSelectedItem();
        if (b == null) return;
        availLabel.setText(b.getAvailableQty() + " / " + b.getTotalQty() + " Available");
        availLabel.setForeground(C_GREEN);
    }

    private void issueBook() {
        try {
            IssueRecord r = new IssueRecord();
            r.setStudentName(nameF.getText());
            r.setStudentPhone(phoneF.getText());
            r.setStudentEmail(emailF.getText());

            Book b = (Book) bookCombo.getSelectedItem();
            r.setBookId(b.getBookId());

            r.setIssueDate(Date.valueOf(issueDateF.getText()));
            r.setDueDate(Date.valueOf(dueDateF.getText()));

            issueDAO.issue(r);

            JOptionPane.showMessageDialog(this, "Book Issued!");

            loadIssues();
            reloadBooks();
            updateAvailLabel();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void reloadBooks() {
        try {
            bookCombo.removeAllItems();
            List<Book> books = bookDAO.getAll();
            for (Book b : books) bookCombo.addItem(b);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void loadIssues() {
        try {
            tableModel.setRowCount(0);
            List<IssueRecord> list = issueDAO.getActive();
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadAll() {
        try {
            tableModel.setRowCount(0);
            List<IssueRecord> list = issueDAO.getAll();
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void searchIssues() {
        try {
            tableModel.setRowCount(0);
            List<IssueRecord> list = issueDAO.search(searchF.getText());
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void clearForm() {
        nameF.setText("");
        phoneF.setText("");
        emailF.setText("");
    }
}