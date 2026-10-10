package library;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class Main {
    static final Color NAVY = new Color(24, 58, 102), BLUE = new Color(42, 101, 173);
    static final Color BACKGROUND = new Color(244, 246, 249), BORDER = new Color(220, 225, 232);
    static final Font TITLE = new Font("SansSerif", Font.BOLD, 25), BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static void main(String[] args) { SwingUtilities.invokeLater(Main::open); }

    static void open() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        JFrame frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setLayout(new BorderLayout());
        frame.add(header(), BorderLayout.NORTH);
        BooksPanel books = new BooksPanel(); MembersPanel members = new MembersPanel(); IssueReturnPanel issues = new IssueReturnPanel();
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("SansSerif", Font.BOLD, 15)); tabs.setBackground(Color.WHITE);
        tabs.addTab("  Books  ", books); tabs.addTab("  Members  ", members); tabs.addTab("  Issue / Return  ", issues);
        tabs.addChangeListener(e -> { if (tabs.getSelectedComponent() == books) books.load(); if (tabs.getSelectedComponent() == members) members.load(); });
        JPanel content = new JPanel(new BorderLayout()); content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(18, 24, 24, 24)); content.add(tabs);
        frame.add(content); frame.setSize(1050, 670); frame.setMinimumSize(new Dimension(850, 520));
        frame.setLocationRelativeTo(null); frame.setVisible(true);
    }

    private static JPanel header() {
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(NAVY); header.setBorder(new EmptyBorder(18, 28, 18, 28));
        JLabel title = new JLabel("Library Management System"); title.setFont(new Font("SansSerif", Font.BOLD, 25)); title.setForeground(Color.WHITE);
        JLabel note = new JLabel("Books, members, and borrowing in one place"); note.setFont(BODY); note.setForeground(new Color(220, 232, 248));
        header.add(title, BorderLayout.NORTH); header.add(note, BorderLayout.SOUTH); return header;
    }

    static JPanel card() {
        JPanel card = new JPanel(); card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(14, 14, 14, 14))); return card;
    }

    static JTextField input(int columns) {
        JTextField field = new JTextField(columns); field.setFont(BODY);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34)); return field;
    }

    static JButton button(String text, boolean primary) {
        JButton button = new JButton(text); button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); button.setMargin(new Insets(8, 14, 8, 14));
        if (primary) { button.setBackground(BLUE); button.setForeground(Color.BLACK); button.setBorder(new EmptyBorder(8, 14, 8, 14)); }
        else { button.setBackground(Color.WHITE); button.setForeground(NAVY); button.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(7, 13, 7, 13))); }
        return button;
    }

    static JTable table(String... columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        JTable table = new JTable(model); table.setFont(BODY); table.setRowHeight(34); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(225, 236, 249)); table.setSelectionForeground(Color.BLACK); table.setGridColor(new Color(235, 238, 242));
        table.setShowVerticalLines(false); table.setFillsViewportHeight(true); table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 240, 246)); table.getTableHeader().setForeground(Color.BLACK); table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        return table;
    }

    static JScrollPane scroll(Component component) {
        JScrollPane scroll = new JScrollPane(component); scroll.setBorder(new LineBorder(BORDER)); scroll.getVerticalScrollBar().setUnitIncrement(16); return scroll;
    }
    static void error(Component parent, Exception error) { JOptionPane.showMessageDialog(parent, error.getMessage(), "Library error", JOptionPane.ERROR_MESSAGE); }
    static int number(JTextField field, String name) throws SQLException {
        try { return Integer.parseInt(field.getText().trim()); } catch (NumberFormatException e) { throw new SQLException(name + " must be a number."); }
    }
}
