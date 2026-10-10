package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;

public class BooksPanel extends JPanel {
    private final JTextField search = Main.input(22);
    private final JTable table = Main.table("Book ID", "Title", "Author", "Category", "Availability");
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();

    public BooksPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));
        add(heading(), BorderLayout.NORTH);
        add(Main.scroll(table), BorderLayout.CENTER);
        setWidths(100, 280, 190, 150, 120);
        load();
    }

    private JPanel heading() {
        JPanel box = new JPanel(new BorderLayout(12, 12));
        box.setBackground(Color.WHITE);
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(Color.WHITE);
        JLabel title = new JLabel("Book Catalog"); title.setFont(Main.TITLE);
        JLabel note = new JLabel("Search, add, or remove books from the collection.");
        note.setFont(Main.BODY); note.setForeground(Color.GRAY);
        text.add(title); text.add(Box.createVerticalStrut(4)); text.add(note);
        box.add(text, BorderLayout.NORTH);
        JPanel actions = Main.card();
        actions.setLayout(new FlowLayout(FlowLayout.LEFT, 9, 0));
        actions.add(new JLabel("Search")); actions.add(search);
        JButton find = Main.button("Search", false); find.addActionListener(e -> load()); actions.add(find);
        JButton all = Main.button("View All", false); all.addActionListener(e -> { search.setText(""); load(); }); actions.add(all);
        JButton add = Main.button("+ Add Book", true); add.addActionListener(e -> showAddForm()); actions.add(add);
        JButton availability = Main.button("Change Availability", false); availability.setForeground(Color.BLACK);
        availability.addActionListener(e -> showAvailabilityDialog()); actions.add(availability);
        JButton delete = Main.button("Delete Selected", false); delete.addActionListener(e -> deleteBook()); actions.add(delete);
        search.addActionListener(e -> load());
        box.add(actions, BorderLayout.SOUTH);
        return box;
    }

    private void showAddForm() {
        JTextField id = Main.input(18), title = Main.input(18), author = Main.input(18);
        JTextField isbn = Main.input(18), category = Main.input(18), copies = Main.input(18);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        addField(form, "Book ID", id); addField(form, "Title", title); addField(form, "Author", author);
        addField(form, "ISBN (optional)", isbn); addField(form, "Category", category); addField(form, "Total copies", copies);
        if (JOptionPane.showConfirmDialog(this, form, "Add Book", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Database.addBook(id.getText(), title.getText(), author.getText(), isbn.getText(), category.getText(),
                    Main.number(copies, "Total copies"));
            JOptionPane.showMessageDialog(this, "Book added to the catalog."); load();
        } catch (Exception e) { Main.error(this, e); }
    }

    private void addField(JPanel panel, String name, JTextField field) {
        panel.add(new JLabel(name)); panel.add(field);
    }

    private void deleteBook() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first."); return; }
        String id = model.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Delete book " + id + "?", "Confirm delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { Database.deleteBook(id); JOptionPane.showMessageDialog(this, "Book deleted."); load(); }
        catch (SQLException e) { Main.error(this, e); }
    }

    private void showAvailabilityDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first."); return; }
        String id = model.getValueAt(row, 0).toString();
        try {
            int[] stock = Database.getBookStock(id);
            if (stock == null) throw new SQLException("Book not found.");
            JTextField bookId = Main.input(16), title = Main.input(16), total = Main.input(16);
            JTextField issued = Main.input(16), available = Main.input(16), newTotal = Main.input(16), newAvailable = Main.input(16);
            bookId.setText(id); bookId.setEditable(false); title.setText(model.getValueAt(row, 1).toString()); title.setEditable(false);
            total.setText(String.valueOf(stock[0])); total.setEditable(false); issued.setText(String.valueOf(stock[1])); issued.setEditable(false);
            available.setText(String.valueOf(stock[2])); available.setEditable(false);
            newTotal.setText(String.valueOf(stock[0])); newAvailable.setText(String.valueOf(stock[2]));
            boolean[] changingAvailable = {false}, availableEdited = {false};
            newAvailable.getDocument().addDocumentListener(changeListener(() -> { if (!changingAvailable[0]) availableEdited[0] = true; }));
            newTotal.getDocument().addDocumentListener(changeListener(() -> {
                if (availableEdited[0]) return;
                try { changingAvailable[0] = true; newAvailable.setText(String.valueOf(Integer.parseInt(newTotal.getText().trim()) - stock[1])); }
                catch (NumberFormatException ignored) { } finally { changingAvailable[0] = false; }
            }));
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10)); form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            addField(form, "Book ID", bookId); addField(form, "Book title", title); addField(form, "Current total copies", total);
            addField(form, "Currently issued copies", issued); addField(form, "Current available copies", available);
            addField(form, "New total copies", newTotal); addField(form, "New available copies", newAvailable);
            Object[] options = {"Save Changes", "Cancel"};
            if (JOptionPane.showOptionDialog(this, form, "Change Availability", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]) != 0) return;
            Database.changeAvailability(id, Main.number(newTotal, "New total copies"), Main.number(newAvailable, "New available copies"));
            JOptionPane.showMessageDialog(this, "Book availability updated."); load();
        } catch (SQLException e) { Main.error(this, e); }
    }

    private DocumentListener changeListener(Runnable change) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { change.run(); }
            public void removeUpdate(DocumentEvent e) { change.run(); }
            public void changedUpdate(DocumentEvent e) { change.run(); }
        };
    }

    void load() {
        try { Database.loadBooks(model, search.getText().trim()); }
        catch (SQLException e) { Main.error(this, e); }
    }

    private void setWidths(int... widths) {
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }
}
