package frontend;

import backend.StudentDatabase;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class FinePanel extends JPanel {
    private final JLabel total = new JLabel("Total Fine: ₹0");
    private final JTable table = StudentMain.createTable(new String[]{"Book", "Due Date", "Submission Date",
            "Late Days", "Fine", "Status"}, 300, 120, 140, 100, 100, 100);
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();

    public FinePanel() {
        setLayout(new BorderLayout(10, 15));
        setBackground(new Color(246, 248, 252));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        total.setFont(new Font("Segoe UI", Font.BOLD, 30));
        add(total, BorderLayout.NORTH);
        add(StudentMain.scroll(table), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(refresh);
        add(footer, BorderLayout.SOUTH);
    }

    public void refresh() {
        try {
            List<StudentDatabase.LoanData> loans = StudentDatabase.getMyBooks(StudentMain.getCurrentRollNo());
            double sum = 0;
            model.setRowCount(0);
            for (StudentDatabase.LoanData loan : loans) {
                sum += loan.fine();
                if (loan.fine() > 0) {
                    model.addRow(new Object[]{loan.title(), loan.dueDate(),
                            loan.submissionDate() == null ? "-" : loan.submissionDate(),
                            loan.lateDays(), "₹" + loan.fine(), loan.status()});
                }
            }
            total.setText(String.format("Total Fine: ₹%.2f", sum));
        } catch (SQLException e) {
            StudentMain.showError(this, e);
        }
    }
}
