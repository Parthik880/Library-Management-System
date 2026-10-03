package frontend;

import backend.BooksDatabase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;

public class BooksPanel extends JPanel {
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Book ID", "Title", "Author", "Availability"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(25);

    public BooksPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(246, 248, 252));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        controls.setOpaque(false);
        searchField.setPreferredSize(new Dimension(280, 38));
        JButton search = new JButton("Search");
        search.addActionListener(e -> searchBooks());
        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> { searchField.setText(""); refresh(); });
        JButton add = new JButton("+ Add Book");
        add.addActionListener(e -> showAddBookDialog());
        controls.add(searchField);
        controls.add(search);
        controls.add(reset);
        controls.add(add);
        header.add(controls, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createTable() {
        table.setRowHeight(42);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths(table, 120, 380, 260, 180);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(createScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setOpaque(false);
        JButton view = new JButton("View Details");
        view.addActionListener(e -> viewBookDetails());
        JButton settings = new JButton("Change Book Settings");
        settings.addActionListener(e -> changeBookSettings());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> deleteBook());
        buttons.add(view);
        buttons.add(settings);
        buttons.add(delete);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    public void refresh() {
        try {
            showBooks(BooksDatabase.getBooks());
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void searchBooks() {
        try {
            String keyword = searchField.getText().trim();
            showBooks(keyword.isEmpty() ? BooksDatabase.getBooks() : BooksDatabase.searchBooks(keyword));
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void showBooks(List<BooksDatabase.BookData> books) {
        tableModel.setRowCount(0);
        for (BooksDatabase.BookData book : books) {
            tableModel.addRow(new Object[]{book.bookId(), book.title(), book.author(), book.status()});
        }
    }

    private void showAddBookDialog() {
        JTextField id = new JTextField(), title = new JTextField(), author = new JTextField();
        JTextField isbn = new JTextField(), category = new JTextField(), copies = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        addField(form, "Book ID:", id);
        addField(form, "Title:", title);
        addField(form, "Author:", author);
        addField(form, "ISBN:", isbn);
        addField(form, "Category:", category);
        addField(form, "Total Copies:", copies);
        if (JOptionPane.showConfirmDialog(this, form, "Add Book", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        try {
            if (id.getText().isBlank() || title.getText().isBlank() || author.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Book ID, title and author are required.");
                return;
            }
            boolean added = BooksDatabase.addBook(id.getText().trim(), title.getText().trim(),
                    author.getText().trim(), isbn.getText().trim(), category.getText().trim(),
                    Integer.parseInt(copies.getText().trim()));
            JOptionPane.showMessageDialog(this, added ? "Book added successfully." : "Could not add book.");
            if (added) refresh();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total copies must be a number.");
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void viewBookDetails() {
        String bookId = selectedBookId();
        if (bookId == null) return;
        try {
            BooksDatabase.BookDetails book = BooksDatabase.getBookDetails(bookId);
            if (book == null) { JOptionPane.showMessageDialog(this, "Book not found."); return; }

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(20, 25, 20, 25));
            addHeading(panel, "Book Details", 20);
            addDetail(panel, "Book ID", book.bookId());
            addDetail(panel, "Title", book.title());
            addDetail(panel, "Author", book.author());
            addDetail(panel, "ISBN", book.isbn());
            addDetail(panel, "Category", book.category());
            addDetail(panel, "Status", book.status());
            panel.add(new JSeparator());
            addHeading(panel, "Stock", 18);
            addDetail(panel, "Total Copies", String.valueOf(book.totalCopies()));
            addDetail(panel, "Available Copies", String.valueOf(book.availableCopies()));
            addDetail(panel, "Issued Copies", String.valueOf(book.issuedCopies()));
            panel.add(new JSeparator());
            addHeading(panel, "Currently Issued To", 18);
            JScrollPane issuedScroll = createScrollPane(createIssuedTable(bookId));
            issuedScroll.setPreferredSize(new Dimension(650, 180));
            panel.add(issuedScroll);

            JScrollPane detailsScroll = createScrollPane(panel);
            detailsScroll.setPreferredSize(new Dimension(700, 600));
            JOptionPane.showMessageDialog(this, detailsScroll, "Book Details", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private JTable createIssuedTable(String bookId) throws SQLException {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Roll No", "Name", "Issue Date", "Due Date", "Status", "Fine"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (BooksDatabase.IssuedStudent student : BooksDatabase.getIssuedStudents(bookId)) {
            model.addRow(new Object[]{student.rollNo(), student.name(), student.issueDate(),
                    student.dueDate(), student.status(), "₹" + student.fine()});
        }
        JTable issuedTable = new JTable(model);
        issuedTable.setRowHeight(30);
        issuedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths(issuedTable, 120, 180, 110, 110, 100, 80);
        return issuedTable;
    }

    private void deleteBook() {
        String bookId = selectedBookId();
        if (bookId == null) return;
        if (JOptionPane.showConfirmDialog(this, "Delete book " + bookId + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            boolean deleted = BooksDatabase.deleteBook(bookId);
            JOptionPane.showMessageDialog(this, deleted ? "Book deleted." :
                    "Could not delete book. The book may have transaction history.");
            if (deleted) refresh();
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void changeBookSettings() {
        String bookId = selectedBookId();
        if (bookId == null) return;
        try {
            BooksDatabase.BookDetails book = BooksDatabase.getBookDetails(bookId);
            if (book == null) {
                JOptionPane.showMessageDialog(this, "Book not found.");
                return;
            }
            JSpinner total = new JSpinner(new SpinnerNumberModel(book.totalCopies(), 0, Integer.MAX_VALUE, 1));
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Book ID:"));
            form.add(new JLabel(book.bookId()));
            form.add(new JLabel("Title:"));
            form.add(new JLabel(book.title()));
            JLabel totalLabel = new JLabel("Total Copies:");
            totalLabel.setLabelFor(total);
            form.add(totalLabel);
            form.add(total);
            form.add(new JLabel("Currently Issued:"));
            form.add(new JLabel(String.valueOf(book.issuedCopies())));
            form.add(new JLabel("Available Copies:"));
            form.add(new JLabel(String.valueOf(book.availableCopies())));
            int result = JOptionPane.showOptionDialog(this, form, "Change Book Settings",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[]{"Save Changes", "Cancel"}, "Save Changes");
            if (result != 0) return;
            total.commitEdit();
            BooksDatabase.updateTotalCopies(bookId, ((Number) total.getValue()).intValue());
            refresh();
        } catch (java.text.ParseException e) {
            JOptionPane.showMessageDialog(this, "Total copies must be a non-negative whole number.");
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private String selectedBookId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book first.");
            return null;
        }
        return tableModel.getValueAt(row, 0).toString();
    }

    private JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void setColumnWidths(JTable target, int... widths) {
        for (int i = 0; i < widths.length; i++) target.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private void addField(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    private void addHeading(JPanel panel, String text, int size) {
        JLabel heading = new JLabel(text);
        heading.setFont(new Font("Segoe UI", Font.BOLD, size));
        panel.add(Box.createVerticalStrut(12));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(8));
    }

    private void addDetail(JPanel panel, String label, String value) {
        JLabel heading = new JLabel(label);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(heading);
        panel.add(new JLabel(value == null || value.isBlank() ? "-" : value));
        panel.add(Box.createVerticalStrut(8));
    }
}
