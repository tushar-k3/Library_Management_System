package librarymanagementsystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LibraSystem Light Design System.
 *
 * Palette  : Warm white background, deep navy sidebar, teal accents.
 * Icons    : Styled text badges (always render on any JVM/OS).
 * Fonts    : Georgia (headings) + Tahoma (body) — both ship with Windows.
 */
public class Theme {

    // ── Colours ──────────────────────────────────────────────────
    static final Color C_APP_BG     = new Color(245, 248, 252);
    static final Color C_WHITE      = Color.WHITE;
    static final Color C_SIDEBAR    = new Color(14,  42,  71);   // deep navy
    static final Color C_SIDEBAR_HV = new Color(22,  62, 100);
    static final Color C_SIDEBAR_AC = new Color(0,  152, 121);   // teal active
    static final Color C_TOPBAR     = Color.WHITE;

    static final Color C_TEAL       = new Color(0,  152, 121);
    static final Color C_TEAL_D     = new Color(0,  115,  90);
    static final Color C_TEAL_BG    = new Color(209, 245, 238);

    static final Color C_GREEN      = new Color(22,  163,  74);
    static final Color C_GREEN_D    = new Color(15,  118,  56);
    static final Color C_GREEN_BG   = new Color(220, 252, 231);

    static final Color C_RED        = new Color(220,  38,  38);
    static final Color C_RED_D      = new Color(185,  28,  28);
    static final Color C_RED_BG     = new Color(254, 226, 226);

    static final Color C_AMBER      = new Color(180, 130,   0);
    static final Color C_AMBER_BG   = new Color(254, 243, 199);

    static final Color C_BLUE       = new Color(37,  99,  235);
    static final Color C_BLUE_BG    = new Color(219, 234, 254);

    static final Color C_PURPLE     = new Color(109,  40, 217);
    static final Color C_PURPLE_BG  = new Color(237, 233, 254);

    static final Color C_TXT_DARK   = new Color(15,  23,  42);
    static final Color C_TXT_BODY   = new Color(51,  65,  85);
    static final Color C_TXT_MUTED  = new Color(100, 116, 139);
    static final Color C_TXT_NAV    = new Color(203, 213, 225);
    static final Color C_BORDER     = new Color(226, 232, 240);
    static final Color C_BORDER_STR = new Color(203, 213, 225);

    // ── Fonts ─────────────────────────────────────────────────────
    static final Font F_APP_TITLE = new Font("Georgia", Font.BOLD,  22);
    static final Font F_LOGO_NAME = new Font("Georgia", Font.BOLD,  17);
    static final Font F_PAGE_HDR  = new Font("Georgia", Font.BOLD,  19);
    static final Font F_SECTION   = new Font("Tahoma",  Font.BOLD,  13);
    static final Font F_LABEL     = new Font("Tahoma",  Font.BOLD,  12);
    static final Font F_BODY      = new Font("Tahoma",  Font.PLAIN, 12);
    static final Font F_SMALL     = new Font("Tahoma",  Font.PLAIN, 11);
    static final Font F_BTN       = new Font("Tahoma",  Font.BOLD,  12);
    static final Font F_NAV       = new Font("Tahoma",  Font.BOLD,  13);
    static final Font F_TABLE_HDR = new Font("Tahoma",  Font.BOLD,  12);
    static final Font F_TABLE     = new Font("Tahoma",  Font.PLAIN, 12);
    static final Font F_STAT_NUM  = new Font("Georgia", Font.BOLD,  34);
    static final Font F_STAT_LBL  = new Font("Tahoma",  Font.PLAIN, 11);
    static final Font F_BADGE     = new Font("Tahoma",  Font.BOLD,  10);

    // ── Icon badges (text that always renders) ────────────────────
    /** A small coloured pill badge used as an icon. */
    static JLabel iconBadge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(F_BADGE); l.setForeground(fg); l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(3, 7, 3, 7));
        l.setPreferredSize(new Dimension(42, 22));
        return l;
    }

    // ── Buttons ───────────────────────────────────────────────────
    static JButton btnPrimary(String text) {
        return makeBtn(text, C_TEAL, C_TEAL_D, Color.WHITE);
    }
    static JButton btnSuccess(String text) {
        return makeBtn(text, C_GREEN, C_GREEN_D, Color.WHITE);
    }
    static JButton btnDanger(String text) {
        return makeBtn(text, C_RED, C_RED_D, Color.WHITE);
    }
    static JButton btnOutline(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BTN); b.setForeground(C_TEAL_D); b.setBackground(C_WHITE);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_TEAL, 1, true), new EmptyBorder(6, 14, 6, 14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(C_TEAL_BG); }
            public void mouseExited (MouseEvent e) { b.setBackground(C_WHITE); }
        });
        return b;
    }
    private static JButton makeBtn(String t, Color bg, Color hover, Color fg) {
        JButton b = new JButton(t);
        b.setFont(F_BTN); b.setForeground(fg); b.setBackground(bg);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 18, 7, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited (MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    // ── Input fields ──────────────────────────────────────────────
    static JTextField field(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(F_BODY); tf.setForeground(C_TXT_DARK); tf.setBackground(C_WHITE);
        tf.setCaretColor(C_TEAL);
        tf.setBorder(border1());
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { tf.setBorder(borderFocus()); }
            public void focusLost  (FocusEvent e) { tf.setBorder(border1()); }
        });
        return tf;
    }

    static JTextArea area(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(F_BODY); ta.setForeground(C_TXT_DARK); ta.setBackground(C_WHITE);
        ta.setCaretColor(C_TEAL); ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(7, 10, 7, 10));
        return ta;
    }

    static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(F_BODY); cb.setBackground(C_WHITE); cb.setForeground(C_TXT_DARK);
        cb.setBorder(new LineBorder(C_BORDER, 1));
        return cb;
    }

    // ── Labels ────────────────────────────────────────────────────
    static JLabel lbl(String text) {
        JLabel l = new JLabel(text); l.setFont(F_LABEL); l.setForeground(C_TXT_BODY);
        return l;
    }
    static JLabel muted(String text) {
        JLabel l = new JLabel(text); l.setFont(F_SMALL); l.setForeground(C_TXT_MUTED);
        return l;
    }

    // ── Table ─────────────────────────────────────────────────────
    static void styleTable(JTable t) {
        t.setFont(F_TABLE); t.setForeground(C_TXT_BODY);
        t.setBackground(C_WHITE); t.setGridColor(C_BORDER);
        t.setRowHeight(34);
        t.setSelectionBackground(C_TEAL_BG);
        t.setSelectionForeground(C_TXT_DARK);
        t.setShowGrid(true); t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);

        t.getTableHeader().setFont(F_TABLE_HDR);
        t.getTableHeader().setBackground(C_SIDEBAR);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0,C_TEAL));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Alternating row renderer
        javax.swing.table.DefaultTableCellRenderer rend =
            new javax.swing.table.DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean sel,
                        boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(table,value,sel,focus,row,col);
                    if (!sel) setBackground(row % 2 == 0 ? C_WHITE : new Color(247,252,255));
                    setBorder(new EmptyBorder(0, 10, 0, 10));
                    return this;
                }
            };
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(rend);
    }

    static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(C_BORDER, 1));
        sp.getViewport().setBackground(C_WHITE);
        return sp;
    }

    // ── Stat card ─────────────────────────────────────────────────
    static JPanel statCard(JLabel numLbl, String label, Color accent, Color accentBg, String ico) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(C_WHITE);
        card.setBorder(new LineBorder(C_BORDER, 1, true));

        JPanel strip = new JPanel();
        strip.setBackground(accent);
        strip.setPreferredSize(new Dimension(0, 4));
        card.add(strip, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(C_WHITE);
        body.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Top-right icon badge
        JLabel ic = new JLabel(ico, SwingConstants.CENTER);
        ic.setFont(F_BADGE); ic.setForeground(accent); ic.setBackground(accentBg);
        ic.setOpaque(true); ic.setBorder(new EmptyBorder(3,8,3,8));
        JPanel topR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topR.setBackground(C_WHITE); topR.add(ic);
        body.add(topR, BorderLayout.NORTH);

        numLbl.setFont(F_STAT_NUM); numLbl.setForeground(accent);
        numLbl.setHorizontalAlignment(SwingConstants.LEFT);
        JLabel lbl = new JLabel(label); lbl.setFont(F_STAT_LBL); lbl.setForeground(C_TXT_MUTED);

        JPanel vals = new JPanel(new BorderLayout(0, 2)); vals.setBackground(C_WHITE);
        vals.add(numLbl, BorderLayout.CENTER); vals.add(lbl, BorderLayout.SOUTH);
        body.add(vals, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // ── Page top bar ──────────────────────────────────────────────
    static JPanel topBar(String pageTitle, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout(0, 2));
        bar.setBackground(C_TOPBAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
            new EmptyBorder(16, 26, 16, 26)));
        JLabel t = new JLabel(pageTitle); t.setFont(F_PAGE_HDR); t.setForeground(C_TXT_DARK);
        JLabel s = new JLabel(subtitle);  s.setFont(F_SMALL);    s.setForeground(C_TXT_MUTED);
        JPanel left = new JPanel(new BorderLayout(0, 2)); left.setBackground(C_TOPBAR);
        left.add(t, BorderLayout.CENTER); left.add(s, BorderLayout.SOUTH);
        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    // ── Section card ──────────────────────────────────────────────
    static JPanel sectionCard() {
        JPanel p = new JPanel(); p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true), new EmptyBorder(20, 22, 20, 22)));
        return p;
    }

    // ── Separator ─────────────────────────────────────────────────
    static JSeparator hSep() {
        JSeparator s = new JSeparator();
        s.setForeground(C_BORDER); s.setBackground(C_BORDER);
        return s;
    }

    // ── Borders ───────────────────────────────────────────────────
    private static Border border1()   {
        return BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true), new EmptyBorder(7,10,7,10));
    }
    private static Border borderFocus(){
        return BorderFactory.createCompoundBorder(
            new LineBorder(C_TEAL, 2, true), new EmptyBorder(6,9,6,9));
    }
}
