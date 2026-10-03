package frontend;

import backend.TransactionDatabase;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class IssueReturnPanel extends JPanel {
    private final JTextField rollField = new JTextField(18);
    private final JTextField bookField = new JTextField(12);
    private final JTable table = Main.createTable(new String[]{
            "Book ID", "Book Title", "Issue Date", "Due Date", "Status", "Fine"}, 120, 300, 120, 120, 100, 100);
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();

    public IssueReturnPanel() {
        setLayout(new BorderLayout(10, 15));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        setBackground(new Color(246, 248, 252));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Issue / Return");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.add(title, BorderLayout.NORTH);
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        form.setOpaque(false);
        JLabel rollLabel = new JLabel("Student Roll No");
        rollLabel.setLabelFor(rollField);
        JLabel bookLabel = new JLabel("Book ID");
        bookLabel.setLabelFor(bookField);
        form.add(rollLabel);
        form.add(rollField);
        form.add(bookLabel);
        form.add(bookField);
        JButton view = new JButton("View Active Issue");
        view.addActionListener(e -> refresh());
        JButton issue = new JButton("Issue Book");
        issue.addActionListener(e -> issueBook());
        JButton submit = new JButton("Return Book");
        submit.addActionListener(e -> returnBook());
        form.add(view);
        form.add(issue);
        form.add(submit);
        header.add(form, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        add(Main.scroll(table), BorderLayout.CENTER);
        rollField.addActionListener(e -> refresh());
    }

    public void refresh() {
        try {
            TransactionDatabase.TransactionData active =
                    TransactionDatabase.getActiveTransaction(rollField.getText().trim());
            model.setRowCount(0);
            if (active != null) {
                model.addRow(new Object[]{active.bookId(), active.title(), active.issueDate(),
                        active.dueDate(), active.status(), "₹" + active.fine()});
                bookField.setText(active.bookId());
            }
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void issueBook() {
        try {
            TransactionDatabase.issueBook(rollField.getText().trim(), bookField.getText().trim());
            JOptionPane.showMessageDialog(this, "Book issued for 14 days.");
            refresh();
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void returnBook() {
        try {
            double fine = TransactionDatabase.returnBook(rollField.getText().trim(), bookField.getText().trim());
            JOptionPane.showMessageDialog(this, String.format("Book returned. Fine: ₹%.2f", fine));
            refresh();
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }
}
