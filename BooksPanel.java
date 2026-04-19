package librarymanagementsystem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import static librarymanagementsystem.Theme.*;

/**
 * Books panel — catalogue management with quantity tracking.
 * Fields: ISBN, Title, Author, Genre, Total Qty.
 * Display: Total Qty, Available Qty, Issued Qty.
 */
public class BooksPanel extends JPanel {

    private final BookDAO dao = new BookDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;

    private static final String[] COLS = {
        "#", "ISBN", "Title", "Author", "Genre",
        "Total Copies", "Available", "Issued", "Added On"
    };

    BooksPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_APP_BG);
        build();
        reload(null);
    }

    // ─────────────────────────────────────────────────────────────
    private void build() {
        // Top bar
        add(topBar("Book Catalogue",
            "Add, edit, delete books  |  Track available & issued copies"),
            BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = buildToolbar();
        JPanel tableWrap = buildTable();

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(C_APP_BG);
        center.setBorder(new EmptyBorder(14, 22, 22, 22));
        center.add(toolbar,   BorderLayout.NORTH);
        center.add(tableWrap, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Status bar
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(C_APP_BG);
        bar.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Left: search
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(C_APP_BG);
        JLabel ico = iconBadge("SRCH", C_TEAL, C_TEAL_BG);
        searchField = field(26);
        JButton searchBtn = btnPrimary("Search");
        JButton clearBtn  = btnOutline("Clear");
        searchBtn.addActionListener(e -> reload(searchField.getText().trim()));
        clearBtn .addActionListener(e -> { searchField.setText(""); reload(null); });
        searchField.addActionListener(e -> reload(searchField.getText().trim()));
        left.add(ico); left.add(searchField); left.add(searchBtn); left.add(clearBtn);
        bar.add(left, BorderLayout.WEST);

        // Right: actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(C_APP_BG);

        JButton addBtn  = btnSuccess("+ Add Book");
        JButton editBtn = btnPrimary("/ Edit");
        JButton delBtn  = btnDanger("x Delete");
        JButton refBtn  = btnOutline("Refresh");

        addBtn .addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn .addActionListener(e -> deleteSelected());
        refBtn .addActionListener(e -> reload(searchField.getText().trim()));

        right.add(addBtn); right.add(editBtn); right.add(delBtn); right.add(refBtn);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        int[] widths = {45, 145, 220, 145, 90, 100, 90, 80, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(C_APP_BG);
        wrap.add(scroll(table), BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 5));
        bar.setBackground(new Color(248, 250, 252));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        JLabel tip = muted("Tip: Double-click a row to edit  |  Issued copies cannot be deleted");
        bar.add(tip);
        return bar;
    }

    // ── Data ─────────────────────────────────────────────────────
    void reload(String kw) {
        try {
            List<Book> books = (kw == null || kw.isBlank())
                ? dao.getAll() : dao.search(kw);
            tableModel.setRowCount(0);
            for (Book b : books) {
                tableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getGenre(),
                    b.getTotalQty(),
                    b.getAvailableQty(),
                    b.getIssuedQty(),
                    b.getDateAdded() != null
                        ? b.getDateAdded().toString().substring(0, 10) : ""
                });
            }
        } catch (SQLException ex) {
            err("Load error: " + ex.getMessage());
        }
    }

    // ── Actions ───────────────────────────────────────────────────
    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Please select a book first."); return; }
        try {
            Book b = dao.getById((int) tableModel.getValueAt(row, 0));
            if (b != null) openForm(b);
        } catch (SQLException ex) { err(ex.getMessage()); }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Please select a book first."); return; }
        String title = (String) tableModel.getValueAt(row, 2);
        if (JOptionPane.showConfirmDialog(this,
            "Delete \"" + title + "\"?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            dao.deleteBook((int) tableModel.getValueAt(row, 0));
            reload(searchField.getText().trim());
            ok("Book deleted.");
        } catch (SQLException ex) {
            err(ex.getMessage());   // message from DAO already explains why
        }
    }

    // ── Form dialog ───────────────────────────────────────────────
    private void openForm(Book existing) {
        boolean isNew = (existing == null);
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            isNew ? "Add New Book" : "Edit Book", true);
        dlg.setResizable(false);

        // ── Dialog header ─────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout(12, 0));
        hdr.setBackground(C_SIDEBAR);
        hdr.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel hIco = iconBadge("BOOK", Color.WHITE, C_TEAL);
        JLabel hTxt = new JLabel(isNew ? "Add New Book" : "Edit Book Details");
        hTxt.setFont(F_SECTION); hTxt.setForeground(Color.WHITE);
        hdr.add(hIco, BorderLayout.WEST);
        hdr.add(hTxt, BorderLayout.CENTER);

        // ── Form fields ───────────────────────────────────────────
        JTextField isbnF  = field(22); isbnF .setText(isNew ? "" : existing.getIsbn());
        JTextField titleF = field(22); titleF.setText(isNew ? "" : existing.getTitle());
        JTextField authF  = field(22); authF .setText(isNew ? "" : existing.getAuthor());
        JTextField genreF = field(22); genreF.setText(isNew ? "" : nvl(existing.getGenre()));
        JTextField qtyF   = field(22); qtyF  .setText(isNew ? "1" : String.valueOf(existing.getTotalQty()));

        // If editing, also show available (editable only to correct data)
        JTextField availF = field(22);
        availF.setText(isNew ? "" : String.valueOf(existing.getAvailableQty()));
        availF.setEnabled(!isNew);   // available comes from triggers; allow correction in edit
        availF.setBackground(isNew ? new Color(245,245,245) : C_WHITE);

        // ── Layout ────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_WHITE);
        form.setBorder(new EmptyBorder(22, 26, 10, 26));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        Object[][] rows = {
            {"ISBN *",           isbnF},
            {"Title *",          titleF},
            {"Author *",         authF},
            {"Genre",            genreF},
            {"Total Copies *",   qtyF},
            {"Available Copies", availF},
        };
        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            form.add(lbl((String) rows[i][0]), g);
            g.gridx = 1; g.weightx = 1;
            form.add((Component) rows[i][1], g);
        }

        // Info note
        g.gridx = 0; g.gridy = rows.length; g.gridwidth = 2;
        JLabel note = muted("* Required  |  Available Copies auto-managed by issue/return triggers.");
        note.setBorder(new EmptyBorder(4, 0, 0, 0));
        form.add(note, g);

        // ── Footer buttons ────────────────────────────────────────
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        foot.setBackground(new Color(248, 250, 252));
        foot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        JButton cancel = btnOutline("Cancel");
        JButton save   = isNew ? btnSuccess("+ Add Book") : btnPrimary("/ Save Changes");
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            // ── Validation ────────────────────────────────────────
            if (isbnF.getText().isBlank())  { warn(dlg, "ISBN is required."); return; }
            if (titleF.getText().isBlank()) { warn(dlg, "Title is required."); return; }
            if (authF.getText().isBlank())  { warn(dlg, "Author is required."); return; }
            int qty;
            try {
                qty = Integer.parseInt(qtyF.getText().trim());
                if (qty < 1) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                warn(dlg, "Total Copies must be a positive number."); return;
            }

            try {
                if (isNew) {
                    dao.addBook(new Book(
                        isbnF.getText().trim(), titleF.getText().trim(),
                        authF.getText().trim(), genreF.getText().trim(), qty));
                } else {
                    existing.setIsbn(isbnF.getText().trim());
                    existing.setTitle(titleF.getText().trim());
                    existing.setAuthor(authF.getText().trim());
                    existing.setGenre(genreF.getText().trim());
                    existing.setTotalQty(qty);
                    if (!availF.getText().isBlank())
                        existing.setAvailableQty(Integer.parseInt(availF.getText().trim()));
                    dao.updateBook(existing);
                }
                reload(searchField.getText().trim());
                dlg.dispose();
                ok(isNew ? "Book added successfully!" : "Book updated successfully!");
            } catch (NumberFormatException nfe2) {
                warn(dlg, "Available Copies must be a number.");
            } catch (SQLException ex) {
                err(dlg, "Database error:\n" + ex.getMessage());
            }
        });
        foot.add(cancel); foot.add(save);

        // ── Assemble ──────────────────────────────────────────────
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(hdr,  BorderLayout.NORTH);
        wrap.add(form, BorderLayout.CENTER);
        wrap.add(foot, BorderLayout.SOUTH);
        dlg.add(wrap);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(460, 0));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Notice", JOptionPane.WARNING_MESSAGE);
    }
    private void warn(Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Validation", JOptionPane.WARNING_MESSAGE);
    }
    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void err(Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void ok(String m) {
        JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    private String nvl(String s) { return s == null ? "" : s; }
}
