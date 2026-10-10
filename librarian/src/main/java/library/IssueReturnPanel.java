package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;

public class IssueReturnPanel extends JPanel {
    private final JTextField roll = Main.input(18);
    private final JTextField book = Main.input(18);
    private final JTable table = Main.table("Book ID", "Book Title", "Issue Date", "Due Date", "Status", "Fine");
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();

    public IssueReturnPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));
        add(heading(), BorderLayout.NORTH);
        add(Main.scroll(table), BorderLayout.CENTER);
        int[] widths = {100, 260, 115, 115, 90, 90};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private JPanel heading() {
        JPanel box = new JPanel(new BorderLayout(12, 12)); box.setBackground(Color.WHITE);
        JPanel text = new JPanel(); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS)); text.setBackground(Color.WHITE);
        JLabel title = new JLabel("Issue & Return"); title.setFont(Main.TITLE);
        JLabel note = new JLabel("Enter a member roll number and book ID to manage the current loan.");
        note.setFont(Main.BODY); note.setForeground(Color.GRAY);
        text.add(title); text.add(Box.createVerticalStrut(4)); text.add(note); box.add(text, BorderLayout.NORTH);

        JPanel form = Main.card(); form.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));
        form.add(new JLabel("Member roll number")); form.add(roll);
        form.add(new JLabel("Book ID")); form.add(book);
        JButton view = Main.button("View Active Issue", false); view.addActionListener(e -> load()); form.add(view);
        JButton issue = Main.button("Issue Book", true); issue.setForeground(Color.BLACK); issue.addActionListener(e -> issueBook()); form.add(issue);
        JButton returned = Main.button("Return Book", false); returned.addActionListener(e -> returnBook()); form.add(returned);
        box.add(form, BorderLayout.SOUTH);
        return box;
    }

    private void issueBook() {
        JTextField memberField = Main.input(18), bookField = Main.input(18);
        memberField.setText(roll.getText().trim()); memberField.setEditable(false);
        bookField.setText(book.getText().trim()); bookField.setEditable(false);
        JTextField issueDateField = Main.input(18), dueDateField = Main.input(18);
        LocalDate today = LocalDate.now();
        issueDateField.setText(today.toString()); dueDateField.setText(today.plusDays(14).toString());
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Student roll number")); form.add(memberField);
        form.add(new JLabel("Book ID")); form.add(bookField);
        form.add(new JLabel("Issue date (YYYY-MM-DD)")); form.add(issueDateField);
        form.add(new JLabel("Due date (YYYY-MM-DD)")); form.add(dueDateField);
        Object[] options = {"Confirm Issue", "Cancel"};
        if (JOptionPane.showOptionDialog(this, form, "Confirm Book Issue", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]) != 0) return;
        try {
            LocalDate issueDate = LocalDate.parse(issueDateField.getText().trim());
            LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());
            if (dueDate.isBefore(issueDate)) throw new SQLException("Due date cannot be before the issue date.");
            Database.issueBook(memberField.getText(), bookField.getText(), issueDate, dueDate);
            JOptionPane.showMessageDialog(this, "Book issued successfully.");
            load();
        } catch (DateTimeParseException e) {
            Main.error(this, new SQLException("Use dates in YYYY-MM-DD format."));
        } catch (SQLException e) { Main.error(this, e); }
    }

    private void returnBook() {
        try {
            double fine = Database.returnBook(roll.getText(), book.getText());
            JOptionPane.showMessageDialog(this, String.format("Book returned. Fine: ₹%.2f", fine));
            load();
        } catch (SQLException e) { Main.error(this, e); }
    }

    private void load() {
        try { Database.loadActiveIssue(model, roll.getText().trim()); }
        catch (SQLException e) { Main.error(this, e); }
    }
}
