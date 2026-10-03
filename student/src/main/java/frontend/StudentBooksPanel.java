package frontend;

import backend.StudentDatabase;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentBooksPanel extends JPanel {
    private final JTextField searchField = new JTextField(25);
    private final JTable table = StudentMain.createTable(new String[]{"Book ID", "Title", "Author", "Category",
            "Availability", "My Status", "Fine"}, 100, 300, 200, 160, 130, 100, 100);
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();

    public StudentBooksPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(246, 248, 252));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.add(title, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        controls.setOpaque(false);
        JButton search = new JButton("Search");
        search.addActionListener(e -> refresh());
        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> { searchField.setText(""); refresh(); });
        controls.add(searchField);
        controls.add(search);
        controls.add(reset);
        header.add(controls, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(StudentMain.scroll(table), BorderLayout.CENTER);
        JButton details = new JButton("View Details");
        details.addActionListener(e -> viewDetails());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(details);
        add(footer, BorderLayout.SOUTH);
    }

    public void refresh() {
        try {
            List<StudentDatabase.BookData> books = StudentDatabase.searchBooks(
                    StudentMain.getCurrentRollNo(), searchField.getText().trim());
            model.setRowCount(0);
            for (StudentDatabase.BookData book : books) {
                model.addRow(new Object[]{book.bookId(), book.title(), book.author(), book.category(),
                        book.availability(), book.myStatus(), "₹" + book.fine()});
            }
        } catch (SQLException e) {
            StudentMain.showError(this, e);
        }
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book first.");
            return;
        }
        try {
            StudentDatabase.BookData book = StudentDatabase.getBookDetails(
                    StudentMain.getCurrentRollNo(), model.getValueAt(row, 0).toString());
            if (book == null) {
                JOptionPane.showMessageDialog(this, "Book not found.");
                return;
            }
            String[] labels = {"Book ID", "Title", "Author", "ISBN", "Category", "Availability"};
            String[] values = {book.bookId(), book.title(), book.author(), book.isbn(), book.category(), book.availability()};
            if (!book.myStatus().equals("-")) {
                labels = new String[]{"Book ID", "Title", "Author", "ISBN", "Category", "Availability",
                        "Issue Date", "Due Date", "Status", "Fine"};
                values = new String[]{book.bookId(), book.title(), book.author(), book.isbn(), book.category(),
                        book.availability(), book.issueDate(), book.dueDate(), book.myStatus(), "₹" + book.fine()};
            }
            StudentMain.showDetails(this, "Book Details", labels, values);
        } catch (SQLException e) {
            StudentMain.showError(this, e);
        }
    }
}
