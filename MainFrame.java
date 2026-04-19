package librarymanagementsystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import static librarymanagementsystem.Theme.*;

public class MainFrame extends JFrame {

    private CardLayout cards;
    private JPanel contentArea;

    private DashboardPanel dashPanel;
    private BooksPanel booksPanel;
    private IssuePanel issuePanel;
    private ReturnPanel returnPanel;

    private JButton activeNavBtn = null;
    private JButton dashBtnRef;

    MainFrame() {
        super("LibraSystem  —  Library Management");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int r = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Exit LibraSystem?", "Exit", JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                    DatabaseConnection.close();
                    System.exit(0);
                }
            }
        });

        setMinimumSize(new Dimension(1100, 700));
        setPreferredSize(new Dimension(1280, 800));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(C_APP_BG);

        JPanel sidebar = buildSidebar();
        JPanel content = buildContent();

        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        activateNavBtn(dashBtnRef, "dashboard");
    }

    // ─────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(C_SIDEBAR);
        sb.setPreferredSize(new Dimension(210, 0));

        // ✅ IMPORTANT FIX (force LEFT alignment)
        sb.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel logo = new JPanel(new BorderLayout(10, 4));
        logo.setBackground(new Color(8, 28, 50));
        logo.setBorder(new EmptyBorder(20, 16, 20, 16));
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);   // ✅ FIX

        JLabel logoIco = new JLabel("LIB", SwingConstants.CENTER);
        logoIco.setFont(new Font("Tahoma", Font.BOLD, 13));
        logoIco.setForeground(new Color(8, 28, 50));
        logoIco.setBackground(C_TEAL);
        logoIco.setOpaque(true);
        logoIco.setBorder(new EmptyBorder(8, 8, 8, 8));
        logoIco.setPreferredSize(new Dimension(44, 44));

        JPanel logoTxt = new JPanel(new BorderLayout(0, 3));
        logoTxt.setBackground(new Color(8, 28, 50));

        JLabel appName = new JLabel("LibraSystem");
        appName.setFont(new Font("Georgia", Font.BOLD, 16));
        appName.setForeground(Color.WHITE);

        JLabel tagline = new JLabel("Library Management");
        tagline.setFont(F_SMALL);
        tagline.setForeground(new Color(148, 163, 184));

        logoTxt.add(appName, BorderLayout.CENTER);
        logoTxt.add(tagline, BorderLayout.SOUTH);

        logo.add(logoIco, BorderLayout.WEST);
        logo.add(logoTxt, BorderLayout.CENTER);
        sb.add(logo);

        sb.add(sep());

        JLabel navLbl = new JLabel("  MENU");
        navLbl.setFont(new Font("Tahoma", Font.BOLD, 10));
        navLbl.setForeground(new Color(71, 85, 105));
        navLbl.setBorder(new EmptyBorder(14, 18, 6, 18));
        navLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        navLbl.setAlignmentX(Component.LEFT_ALIGNMENT);  // ✅ FIX
        sb.add(navLbl);

        JButton dashBtn = navBtn("Dashboard", "DSH", new Color(99, 102, 241), "dashboard");
        JButton booksBtn = navBtn("Books", "BKS", C_TEAL, "books");
        JButton issueBtn = navBtn("Issue Book", "ISS", C_GREEN, "issue");
        JButton returnBtn = navBtn("Return Book", "RET", C_AMBER, "return");

        dashBtnRef = dashBtn;

        sb.add(dashBtn);
        sb.add(booksBtn);
        sb.add(issueBtn);
        sb.add(returnBtn);

        sb.add(Box.createVerticalGlue());
        sb.add(sep());

        JLabel footer = new JLabel("v1.0  |  2025");
        footer.setFont(F_SMALL);
        footer.setForeground(new Color(71, 85, 105));
        footer.setBorder(new EmptyBorder(12, 18, 14, 18));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT); // ✅ FIX
        sb.add(footer);

        return sb;
    }

    private JButton navBtn(String label, String icoText, Color icoColor, String card) {

        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(10, 0));
        btn.setBackground(C_SIDEBAR);
        btn.setForeground(C_TXT_NAV);
        btn.setFont(F_NAV);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);   // ✅ KEY FIX

        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ic = new JLabel(icoText, SwingConstants.CENTER);
        ic.setFont(new Font("Tahoma", Font.BOLD, 10));
        ic.setForeground(Color.WHITE);
        ic.setBackground(icoColor);
        ic.setOpaque(true);
        ic.setBorder(new EmptyBorder(4, 6, 4, 6));
        ic.setPreferredSize(new Dimension(40, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(F_NAV);
        lbl.setForeground(C_TXT_NAV);

        btn.add(ic, BorderLayout.WEST);
        btn.add(lbl, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(C_SIDEBAR_HV);
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(C_SIDEBAR);
            }
        });

        btn.addActionListener(e -> activateNavBtn(btn, card));

        return btn;
    }

    private void activateNavBtn(JButton btn, String card) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(C_SIDEBAR);
            for (Component c : activeNavBtn.getComponents())
                if (c instanceof JLabel) ((JLabel) c).setForeground(C_TXT_NAV);
        }

        btn.setBackground(C_SIDEBAR_AC);
        for (Component c : btn.getComponents())
            if (c instanceof JLabel) ((JLabel) c).setForeground(Color.WHITE);

        activeNavBtn = btn;

        cards.show(contentArea, card);

        if ("dashboard".equals(card) && dashPanel != null) dashPanel.refresh();
        if ("issue".equals(card) && issuePanel != null) issuePanel.reloadBooks();
        if ("return".equals(card) && returnPanel != null) returnPanel.load();
    }

    private Component sep() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private JPanel buildContent() {
        cards = new CardLayout();
        contentArea = new JPanel(cards);
        contentArea.setBackground(C_APP_BG);

        dashPanel = new DashboardPanel();
        booksPanel = new BooksPanel();
        issuePanel = new IssuePanel();
        returnPanel = new ReturnPanel();

        contentArea.add(dashPanel, "dashboard");
        contentArea.add(booksPanel, "books");
        contentArea.add(issuePanel, "issue");
        contentArea.add(returnPanel, "return");

        return contentArea;
    }
}