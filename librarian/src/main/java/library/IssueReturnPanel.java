package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
        JButton issue = Main.button("Issue Book", true); issue.addActionListener(e -> issueBook()); form.add(issue);
        JButton returned = Main.button("Return Book", false); returned.addActionListener(e -> returnBook()); form.add(returned);
        box.add(form, BorderLayout.SOUTH);
        return box;
    }

    private void issueBook() {
        try {
            Database.issueBook(roll.getText(), book.getText());
            JOptionPane.showMessageDialog(this, "Book issued. The due date is 14 days from today.");
            load();
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
