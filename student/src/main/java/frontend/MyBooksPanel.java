package frontend;

import backend.StudentDatabase;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyBooksPanel extends JPanel {
    private final JTable table = StudentMain.createTable(new String[]{"Book ID", "Title", "Issue Date",
            "Due Date", "Submission Date", "Status", "Fine"}, 100, 300, 120, 120, 140, 100, 100);
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();
    private List<StudentDatabase.LoanData> loans = new ArrayList<>();

    public MyBooksPanel() {
        setLayout(new BorderLayout(10, 15));
        setBackground(new Color(246, 248, 252));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        JLabel title = new JLabel("My Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        add(title, BorderLayout.NORTH);
        add(StudentMain.scroll(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JButton details = new JButton("View Details");
        details.addActionListener(e -> viewDetails());
        buttons.add(refresh);
        buttons.add(details);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        try {
            loans = StudentDatabase.getMyBooks(StudentMain.getCurrentRollNo());
            model.setRowCount(0);
            for (StudentDatabase.LoanData loan : loans) {
                model.addRow(new Object[]{loan.bookId(), loan.title(), loan.issueDate(), loan.dueDate(),
                        loan.submissionDate() == null ? "-" : loan.submissionDate(), loan.status(), "₹" + loan.fine()});
            }
        } catch (SQLException e) {
            StudentMain.showError(this, e);
        }
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a transaction first.");
            return;
        }
        long selectedId = loans.get(row).id();
        try {
            // Find the same transaction again, with its current status and fine.
            for (StudentDatabase.LoanData loan : StudentDatabase.getMyBooks(StudentMain.getCurrentRollNo())) {
                if (loan.id() == selectedId) {
                    StudentMain.showDetails(this, "My Book Details",
                            new String[]{"Book Details", "Book ID", "Title", "Author", "Category", "Issue Details",
                                    "Issue Date", "Due Date", "Submission Date", "Status", "Fine"},
                            new String[]{null, loan.bookId(), loan.title(), loan.author(), loan.category(), null,
                                    loan.issueDate(), loan.dueDate(), loan.submissionDate(), loan.status(), "₹" + loan.fine()});
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Transaction no longer exists. Refresh My Books.");
        } catch (SQLException e) {
            StudentMain.showError(this, e);
        }
    }
}
