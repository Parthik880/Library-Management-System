package frontend;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        SwingUtilities.invokeLater(Main::open);
    }

    public static void open() {
        // ponytail: synchronous JDBC for a local mini-project; use SwingWorker if queries become slow.
        JFrame frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 760);
        frame.setMinimumSize(new Dimension(1100, 650));
        frame.setLocationRelativeTo(null);

        CardLayout layout = new CardLayout();
        JPanel content = new JPanel(layout);
        DashboardPanel dashboard = new DashboardPanel();
        MembersPanel members = new MembersPanel();
        BooksPanel books = new BooksPanel();
        IssueReturnPanel issue = new IssueReturnPanel();
        HistoryPanel history = new HistoryPanel();
        String[] names = {"Dashboard", "Members", "Books", "Issue / Return", "Transaction History"};
        JPanel[] pages = {dashboard, members, books, issue, history};

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(25, 18, 25, 18));
        sidebar.setBackground(new Color(23, 43, 77));
        JLabel logo = new JLabel("LIBRARY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(35));

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            content.add(pages[i], name);
            JButton button = new JButton(name);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBackground(new Color(23, 43, 77));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                layout.show(content, name);
                switch (name) {
                    case "Dashboard": dashboard.refresh(); break;
                    case "Members": members.refresh(); break;
                    case "Books": books.refresh(); break;
                    case "Issue / Return": issue.refresh(); break;
                    case "Transaction History": history.refresh(); break;
                }
            });
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(8));
        }
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(content, BorderLayout.CENTER);
        dashboard.refresh();
        frame.setVisible(true);
    }

    static JTable createTable(String[] columns, int... widths) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return table;
    }

    static JScrollPane scroll(Component content) {
        JScrollPane scroll = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    static void showError(Component parent, SQLException error) {
        JOptionPane.showMessageDialog(parent, error.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
