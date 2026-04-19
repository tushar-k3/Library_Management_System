package librarymanagementsystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static librarymanagementsystem.Theme.*;

/** Dashboard — live stat cards + quick-start guide. */
public class DashboardPanel extends JPanel {

    private final BookDAO  bookDAO  = new BookDAO();
    private final IssueDAO issueDAO = new IssueDAO();

    private final JLabel titlesNum  = new JLabel("—");
    private final JLabel copiesNum  = new JLabel("—");
    private final JLabel availNum   = new JLabel("—");
    private final JLabel issuedNum  = new JLabel("—");
    private final JLabel activeNum  = new JLabel("—");
    private       JLabel dateLbl;

    DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(C_APP_BG);
        build();
        refresh();
    }

    private void build() {
        // Top bar
        JPanel tb = topBar("Dashboard", "Library overview at a glance");
        dateLbl = muted("");
        JButton refBtn = btnPrimary("Refresh");
        refBtn.addActionListener(e -> refresh());
        JPanel tbRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        tbRight.setBackground(C_TOPBAR);
        tbRight.add(dateLbl); tbRight.add(refBtn);
        tb.add(tbRight, BorderLayout.EAST);
        add(tb, BorderLayout.NORTH);

        // Scrollable body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_APP_BG);
        body.setBorder(new EmptyBorder(22, 24, 24, 24));

        // Stat cards row 1 (4 cards)
        JLabel s1 = new JLabel("Book Statistics");
        s1.setFont(F_SECTION); s1.setForeground(C_TXT_DARK);
        s1.setAlignmentX(LEFT_ALIGNMENT);
        s1.setBorder(new EmptyBorder(0, 0, 12, 0));
        body.add(s1);

        JPanel row1 = new JPanel(new GridLayout(1, 4, 14, 0));
        row1.setBackground(C_APP_BG);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row1.setAlignmentX(LEFT_ALIGNMENT);
        row1.add(statCard(titlesNum, "Total Titles",      C_TEAL,   C_TEAL_BG,   "BKTS"));
        row1.add(statCard(copiesNum, "Total Copies",      C_BLUE,   C_BLUE_BG,   "COPY"));
        row1.add(statCard(availNum,  "Available Copies",  C_GREEN,  C_GREEN_BG,  "AVBL"));
        row1.add(statCard(issuedNum, "Issued Copies",     C_AMBER,  C_AMBER_BG,  "ISSD"));
        body.add(row1);

        body.add(Box.createVerticalStrut(14));

        // Stat cards row 2 (1 card)
        JLabel s2 = new JLabel("Issue Statistics");
        s2.setFont(F_SECTION); s2.setForeground(C_TXT_DARK);
        s2.setAlignmentX(LEFT_ALIGNMENT);
        s2.setBorder(new EmptyBorder(0, 0, 12, 0));
        body.add(s2);

        JPanel row2 = new JPanel(new GridLayout(1, 4, 14, 0));
        row2.setBackground(C_APP_BG);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.add(statCard(activeNum, "Active Issues", C_RED, C_RED_BG, "ACTV"));
        // Padding panels
        row2.add(new JPanel() {{ setBackground(C_APP_BG); }});
        row2.add(new JPanel() {{ setBackground(C_APP_BG); }});
        row2.add(new JPanel() {{ setBackground(C_APP_BG); }});
        body.add(row2);

        body.add(Box.createVerticalStrut(22));

        // Guide card
        body.add(buildGuide());

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.getViewport().setBackground(C_APP_BG);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildGuide() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(C_WHITE);
        card.setBorder(new LineBorder(C_BORDER, 1, true));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        // Header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        hdr.setBackground(new Color(248,250,252));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_BORDER));
        JPanel al = new JPanel(); al.setBackground(C_TEAL); al.setPreferredSize(new Dimension(4,30));
        hdr.add(al);
        JLabel t = new JLabel("How to Use LibraSystem");
        t.setFont(F_SECTION); t.setForeground(C_SIDEBAR);
        hdr.add(t);
        card.add(hdr, BorderLayout.NORTH);

        // Steps
        JPanel steps = new JPanel(new GridLayout(1, 3, 14, 0));
        steps.setBackground(C_WHITE);
        steps.setBorder(new EmptyBorder(14, 18, 14, 18));
        steps.add(guideStep("Step 1 — Books",
            "Go to the Books tab.\nAdd books with ISBN, title, author and genre.\n"
            + "Set total copies. Available qty is auto-tracked.",
            C_TEAL, C_TEAL_BG));
        steps.add(guideStep("Step 2 — Issue",
            "Go to the Issue tab.\nEnter student name, phone, email.\n"
            + "Pick the book from the dropdown.\nSet a due date and click Issue.",
            C_GREEN, C_GREEN_BG));
        steps.add(guideStep("Step 3 — Return",
            "Go to the Return tab.\nSelect the issue record from the table.\n"
            + "Set return date. Fine = Rs 10/day if overdue.\n"
            + "Click Return to process.",
            C_AMBER, C_AMBER_BG));
        card.add(steps, BorderLayout.CENTER);
        return card;
    }

    private JPanel guideStep(String title, String body, Color accent, Color bg) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accent, 1, true), new EmptyBorder(12, 14, 12, 14)));
        JLabel t = new JLabel(title); t.setFont(F_SECTION); t.setForeground(accent);
        JTextArea b = new JTextArea(body);
        b.setFont(F_SMALL); b.setForeground(C_TXT_BODY);
        b.setEditable(false); b.setBackground(bg);
        b.setLineWrap(true); b.setWrapStyleWord(true);
        p.add(t, BorderLayout.NORTH); p.add(b, BorderLayout.CENTER);
        return p;
    }

    void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                int[] s = bookDAO.stats();
                titlesNum.setText(String.valueOf(s[0]));
                copiesNum.setText(String.valueOf(s[1]));
                availNum .setText(String.valueOf(s[2]));
                issuedNum.setText(String.valueOf(s[3]));
                activeNum.setText(String.valueOf(issueDAO.activeCount()));
                dateLbl.setText(LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + "   ");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Stats error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
