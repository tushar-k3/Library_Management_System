package librarymanagementsystem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static librarymanagementsystem.Theme.*;

/**
 * Return panel.
 *  - Shows all active (Issued / Overdue) records.
 *  - Select a row and click Return to process.
 *  - Return date defaults to today; fine = Rs 10/day if overdue.
 *  - Fine is calculated and shown before confirmation.
 */
public class ReturnPanel extends JPanel {

    private final IssueDAO issueDAO = new IssueDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchF;
    private JLabel            selectedInfoLbl;

    private static final String[] COLS = {
        "Rec #", "Book Title", "Student Name", "Phone",
        "Issue Date", "Due Date", "Status", "Days Left / Overdue"
    };

    ReturnPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_APP_BG);
        build();
        load();
    }

    // ─────────────────────────────────────────────────────────────
    private void build() {
        add(topBar("Return Book",
            "Select an active issue record, verify the return date, then click Return"),
            BorderLayout.NORTH);

        // Return action card + table together
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(C_APP_BG);
        body.setBorder(new EmptyBorder(14, 22, 22, 22));

        body.add(buildActionCard(), BorderLayout.NORTH);
        body.add(buildTable(),      BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ── Action card (return date + button) ────────────────────────
    private JPanel buildActionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(C_WHITE);
        card.setBorder(new LineBorder(C_BORDER, 1, true));

        // Header strip
        JPanel ch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ch.setBackground(new Color(248, 250, 252));
        ch.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        JPanel accentLine = new JPanel();
        accentLine.setBackground(C_AMBER);
        accentLine.setPreferredSize(new Dimension(4, 36));
        ch.add(accentLine);

        JLabel chl = new JLabel("Process Book Return");
        chl.setFont(F_SECTION); chl.setForeground(C_SIDEBAR);
        ch.add(chl);
        card.add(ch, BorderLayout.NORTH);

        // Controls row
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        ctrl.setBackground(C_WHITE);

        ctrl.add(lbl("Return Date:"));
        JTextField retDateF = field(14);
        retDateF.setText(LocalDate.now().toString());
        ctrl.add(retDateF);

        ctrl.add(muted("  (YYYY-MM-DD)"));

        // Selected record display
        selectedInfoLbl = new JLabel("No record selected — click a row above");
        selectedInfoLbl.setFont(F_SMALL);
        selectedInfoLbl.setForeground(C_TXT_MUTED);
        selectedInfoLbl.setBorder(new EmptyBorder(0, 20, 0, 0));
        ctrl.add(selectedInfoLbl);

        card.add(ctrl, BorderLayout.CENTER);

        // Button
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(new Color(248, 250, 252));
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

        JButton refreshBtn = btnOutline("Refresh");
        JButton returnBtn  = btnSuccess("Return Book");

        refreshBtn.addActionListener(e -> load());
        returnBtn .addActionListener(e -> processReturn(retDateF));

        btnRow.add(refreshBtn); btnRow.add(returnBtn);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ── Issues table ──────────────────────────────────────────────
    private JPanel buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_APP_BG);

        // Toolbar
        JPanel tb = new JPanel(new BorderLayout(10, 0));
        tb.setBackground(C_APP_BG);
        tb.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel heading = new JLabel("Active Issue Records  (select a row to return)");
        heading.setFont(F_SECTION); heading.setForeground(C_TXT_DARK);
        tb.add(heading, BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        ctrl.setBackground(C_APP_BG);
        searchF = field(18);
        JButton sb = btnPrimary("Search");
        JButton ab = btnOutline("All Records");
        sb.addActionListener(e -> search());
        ab.addActionListener(e -> loadAll());
        ctrl.add(searchF); ctrl.add(sb); ctrl.add(ab);
        tb.add(ctrl, BorderLayout.EAST);
        panel.add(tb, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        int[] w = {65, 210, 150, 105, 100, 100, 80, 120};
        for (int i = 0; i < w.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // Update info label on row selection
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                selectedInfoLbl.setText("No record selected — click a row above");
                selectedInfoLbl.setForeground(C_TXT_MUTED);
            } else {
                String book   = (String) tableModel.getValueAt(row, 1);
                String stud   = (String) tableModel.getValueAt(row, 2);
                Object due    = tableModel.getValueAt(row, 5);
                String status = (String) tableModel.getValueAt(row, 6);
                selectedInfoLbl.setText(
                    "Selected: " + book + " | Student: " + stud +
                    " | Due: " + due + " | Status: " + status);
                selectedInfoLbl.setForeground(
                    "Overdue".equals(status) ? C_RED : C_GREEN_D);
            }
        });

        panel.add(scroll(table), BorderLayout.CENTER);

        // Status tip
        JPanel tip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        tip.setBackground(C_APP_BG);
        tip.add(muted("Fine = Rs 10 per day overdue  |  Overdue records shown in red"));
        panel.add(tip, BorderLayout.SOUTH);
        return panel;
    }

    // ── Return action ─────────────────────────────────────────────
    private void processReturn(JTextField retDateF) {
        int row = table.getSelectedRow();
        if (row < 0) {
            warn("Please select a record from the table first."); return;
        }

        String statusVal = (String) tableModel.getValueAt(row, 6);
        if ("Returned".equals(statusVal)) {
            warn("This book has already been returned."); return;
        }

        int recordId = (int) tableModel.getValueAt(row, 0);
        Date returnDate;
        try {
            returnDate = Date.valueOf(retDateF.getText().trim());
        } catch (IllegalArgumentException ex) {
            warn("Return date must be in YYYY-MM-DD format."); return;
        }

        // Peek at due date to show fine BEFORE confirming
        Object dueDateObj = tableModel.getValueAt(row, 5);
        String bookTitle  = (String) tableModel.getValueAt(row, 1);
        String studName   = (String) tableModel.getValueAt(row, 2);

        BigDecimal previewFine = BigDecimal.ZERO;
        try {
            Date due = Date.valueOf(dueDateObj.toString());
            if (returnDate.after(due)) {
                long days = (returnDate.getTime() - due.getTime()) / (1000L * 60 * 60 * 24);
                previewFine = new BigDecimal("10").multiply(new BigDecimal(days));
            }
        } catch (Exception ignored) {}

        // Confirmation dialog with fine info
        String fineMsg = previewFine.compareTo(BigDecimal.ZERO) == 0
            ? "No fine (returned on time)."
            : "FINE: Rs " + previewFine.toPlainString() + "  (" +
              daysBetween(dueDateObj.toString(), retDateF.getText().trim()) + " day(s) overdue at Rs 10/day)";

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm Return:\n\n"
            + "Book    : " + bookTitle + "\n"
            + "Student : " + studName  + "\n"
            + "Return  : " + returnDate + "\n\n"
            + fineMsg,
            "Confirm Return", JOptionPane.YES_NO_OPTION,
            previewFine.compareTo(BigDecimal.ZERO) > 0
                ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            BigDecimal fine = issueDAO.returnBook(recordId, returnDate);
            String msg = "Book returned successfully!\n\n"
                + "Book    : " + bookTitle  + "\n"
                + "Student : " + studName   + "\n"
                + "Returned: " + returnDate + "\n\n";
            if (fine.compareTo(BigDecimal.ZERO) > 0)
                msg += "Fine Due: Rs " + fine.toPlainString() + "\n(Please collect from student)";
            else
                msg += "No fine applicable.";

            JOptionPane.showMessageDialog(this, msg, "Returned", JOptionPane.INFORMATION_MESSAGE);
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Return failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Data loaders ──────────────────────────────────────────────
    void load() {
        try { fill(issueDAO.getActive()); }
        catch (SQLException ex) { err(ex.getMessage()); }
    }

    private void loadAll() {
        try { fill(issueDAO.getAll()); }
        catch (SQLException ex) { err(ex.getMessage()); }
    }

    private void search() {
        try { fill(issueDAO.search(searchF.getText().trim())); }
        catch (SQLException ex) { err(ex.getMessage()); }
    }

    private void fill(List<IssueRecord> list) {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (IssueRecord r : list) {
            // Calculate days left / overdue
            String daysInfo = "";
            if (r.getDueDate() != null) {
                LocalDate due = r.getDueDate().toLocalDate();
                long diff = java.time.temporal.ChronoUnit.DAYS.between(today, due);
                if (r.getReturnDate() != null) {
                    daysInfo = "Returned";
                } else if (diff >= 0) {
                    daysInfo = diff + " day(s) left";
                } else {
                    daysInfo = Math.abs(diff) + " day(s) OVERDUE";
                }
            }
            tableModel.addRow(new Object[]{
                r.getRecordId(),
                r.getBookTitle(),
                r.getStudentName(),
                r.getStudentPhone(),
                r.getIssueDate(),
                r.getDueDate(),
                r.getStatus(),
                daysInfo
            });
        }

        // Colour overdue rows red
        javax.swing.table.DefaultTableCellRenderer coloured =
            new javax.swing.table.DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable tbl, Object val, boolean sel,
                        boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(tbl,val,sel,focus,row,col);
                    if (!sel) {
                        String st = (String) tableModel.getValueAt(row, 6);
                        if ("Overdue".equals(st))
                            setBackground(new Color(255, 243, 243));
                        else if ("Returned".equals(st))
                            setBackground(new Color(245, 255, 248));
                        else
                            setBackground(row % 2 == 0 ? C_WHITE : new Color(247,252,255));
                    }
                    setBorder(new EmptyBorder(0, 10, 0, 10));
                    return this;
                }
            };
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(coloured);
    }

    private long daysBetween(String dueDateStr, String retDateStr) {
        try {
            Date due = Date.valueOf(dueDateStr);
            Date ret = Date.valueOf(retDateStr);
            return Math.max(0, (ret.getTime() - due.getTime()) / (1000L * 60 * 60 * 24));
        } catch (Exception e) { return 0; }
    }

    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Notice", JOptionPane.WARNING_MESSAGE);
    }
    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
