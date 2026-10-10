package student;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentView {
    private static final Color NAVY = new Color(24, 58, 102), BORDER = new Color(220, 225, 232);
    private static String rollNo;
    private static final JTextField search = new JTextField(20);
    private static final JTable availableTable = table("Book Name", "Author", "Available Copies");
    private static final JTable issuedTable = table("Book Name", "Issue Date", "Due Date", "Fine");
    public static void main(String[] args) {
        rollNo = JOptionPane.showInputDialog(null, "Enter your roll number:"); if (rollNo == null || rollNo.isBlank()) return;
        SwingUtilities.invokeLater(StudentView::open);
    }

    private static void open() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        JFrame frame = new JFrame("Library Student View"); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setLayout(new BorderLayout());
        frame.add(header(frame), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("  Available Books  ", availableTab(frame)); tabs.addTab("  My Issued Books  ", tab(issuedTable, "View Details", frame));
        JPanel content = new JPanel(new BorderLayout()); content.setBorder(new EmptyBorder(18, 22, 22, 22)); content.setBackground(new Color(244, 246, 249)); content.add(tabs);
        frame.add(content); frame.setSize(780, 500); frame.setLocationRelativeTo(null); frame.setVisible(true);
        new javax.swing.Timer(3000, e -> {
            loadAvailable(frame);
            loadIssued(frame);
        }).start();
    }

    private static JPanel header(Component parent) {
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(NAVY); header.setBorder(new EmptyBorder(17, 24, 17, 24));
        JLabel title = new JLabel("Library Student View"); title.setFont(new Font("SansSerif", Font.BOLD, 23)); title.setForeground(Color.WHITE);
        JLabel user = new JLabel("Signed in as: " + rollNo.trim()); user.setFont(new Font("SansSerif", Font.PLAIN, 14)); user.setForeground(new Color(220, 232, 248));
        JPanel labels = new JPanel(); labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS)); labels.setBackground(NAVY); labels.add(title); labels.add(user);
        JButton refresh = new JButton("Refresh"); refresh.setForeground(Color.BLACK); refresh.setFont(new Font("SansSerif", Font.BOLD, 13));
        refresh.addActionListener(e -> { loadAvailable(parent); loadIssued(parent); });
        header.add(labels, BorderLayout.WEST); header.add(refresh, BorderLayout.EAST); return header;
    }

    private static JPanel availableTab(Component parent) {
        JPanel panel = tab(availableTable, null, parent);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT)); controls.setBackground(Color.WHITE);
        controls.add(new JLabel("Search title or author")); controls.add(search);
        JButton find = new JButton("Search"); find.addActionListener(e -> loadAvailable(parent)); controls.add(find);
        JButton all = new JButton("View All"); all.addActionListener(e -> { search.setText(""); loadAvailable(parent); }); controls.add(all);
        search.addActionListener(e -> loadAvailable(parent)); panel.add(controls, BorderLayout.NORTH);
        return panel;
    }

    private static JPanel tab(JTable table, String buttonText, Component parent) {
        JPanel panel = new JPanel(new BorderLayout(0, 12)); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(18, 18, 18, 18)); panel.add(scroll(table));
        if (buttonText != null) { JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setBackground(Color.WHITE);
            JButton details = new JButton(buttonText); details.setFont(new Font("SansSerif", Font.BOLD, 13)); details.addActionListener(e -> viewDetails(parent)); actions.add(details); panel.add(actions, BorderLayout.SOUTH); }
        return panel;
    }

    private static JTable table(String... columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        JTable table = new JTable(model); table.setFont(new Font("SansSerif", Font.PLAIN, 14)); table.setRowHeight(34); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true); table.setGridColor(new Color(235, 238, 242)); table.setShowVerticalLines(false); table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 240, 246)); table.getTableHeader().setForeground(Color.BLACK); table.getTableHeader().setPreferredSize(new Dimension(0, 36)); return table;
    }

    private static JScrollPane scroll(JTable table) { JScrollPane scroll = new JScrollPane(table); scroll.setBorder(new LineBorder(BORDER)); scroll.getVerticalScrollBar().setUnitIncrement(16); return scroll; }
    private static Connection connect() throws SQLException {
        String url = System.getenv("LIBRARY_DB_URL"), user = System.getenv("LIBRARY_DB_USER"), password = System.getenv("LIBRARY_DB_PASSWORD");
        if (url == null || url.isBlank()) url = "jdbc:postgresql://localhost:5432/library_db"; if (user == null || user.isBlank()) user = "postgres";
        return DriverManager.getConnection(url, user, password);
    }

    private static void loadAvailable(Component parent) {
        String sql = "SELECT title,author,available_copies FROM books WHERE available_copies>0 AND (title ILIKE ? OR author ILIKE ?) ORDER BY title";
        DefaultTableModel model = (DefaultTableModel) availableTable.getModel(); model.setRowCount(0);
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + search.getText().trim() + "%"); ps.setString(2, "%" + search.getText().trim() + "%");
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3)}); }
        }
        catch (SQLException e) { error(parent, e); }
    }

    private static void loadIssued(Component parent) {
        String sql = "SELECT b.title,t.issue_date,t.due_date,GREATEST(CURRENT_DATE-t.due_date,0)*5 FROM transactions t JOIN books b ON b.book_id=t.book_id WHERE t.roll_no=? AND t.submission_date IS NULL";
        DefaultTableModel model = (DefaultTableModel) issuedTable.getModel(); model.setRowCount(0);
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) { ps.setString(1, rollNo.trim()); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getDate(2), rs.getDate(3), "₹" + rs.getInt(4)}); } }
        catch (SQLException e) { error(parent, e); }
    }

    private static void viewDetails(Component parent) {
        int row = issuedTable.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(parent, "Select an issued book first."); return; }
        DefaultTableModel model = (DefaultTableModel) issuedTable.getModel();
        String text = "Book name: " + model.getValueAt(row, 0) + "\nIssue date: " + model.getValueAt(row, 1) + "\nExpiry date: " + model.getValueAt(row, 2) + "\nFine: " + model.getValueAt(row, 3);
        JOptionPane.showMessageDialog(parent, text, "Book Details", JOptionPane.INFORMATION_MESSAGE);
    }
    private static void error(Component parent, SQLException e) { JOptionPane.showMessageDialog(parent, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE); }
}
