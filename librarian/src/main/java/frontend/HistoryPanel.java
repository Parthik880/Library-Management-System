package frontend;

import backend.TransactionDatabase;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class HistoryPanel extends JPanel {
    private final JTextField searchField = new JTextField(25);
    private final JTable table = Main.createTable(new String[]{"Transaction ID", "Roll No", "Student Name",
            "Book ID", "Book Title", "Issue Date", "Due Date", "Submission Date", "Status", "Fine"},
            120, 150, 180, 100, 300, 120, 120, 140, 100, 100);
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 35, 30, 35));
        setBackground(new Color(246, 248, 252));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.add(title, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        controls.setOpaque(false);
        JButton search = new JButton("Search");
        search.addActionListener(e -> refresh());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> { searchField.setText(""); refresh(); });
        controls.add(searchField);
        controls.add(search);
        controls.add(refresh);
        header.add(controls, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(Main.scroll(table), BorderLayout.CENTER);
    }

    public void refresh() {
        try {
            java.util.List<TransactionDatabase.TransactionData> rows =
                    TransactionDatabase.searchTransactionHistory(searchField.getText().trim());
            model.setRowCount(0);
            for (TransactionDatabase.TransactionData row : rows) {
                model.addRow(new Object[]{row.id(), row.rollNo(), row.name(), row.bookId(), row.title(),
                        row.issueDate(), row.dueDate(), row.submissionDate() == null ? "-" : row.submissionDate(),
                        row.status(), "₹" + row.fine()});
            }
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }
}
