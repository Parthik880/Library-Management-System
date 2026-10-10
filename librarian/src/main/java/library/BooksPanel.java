package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    void load() {
        try { Database.loadBooks(model, search.getText().trim()); }
        catch (SQLException e) { Main.error(this, e); }
    }

    private void setWidths(int... widths) {
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }
}
