package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class MembersPanel extends JPanel {
    private final JTextField search = Main.input(22);
    private final JTable table = Main.table("Roll No", "Name", "Email", "Phone", "Year", "Branch");
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();

    public MembersPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));
        add(heading(), BorderLayout.NORTH);
        add(Main.scroll(table), BorderLayout.CENTER);
        int[] widths = {120, 180, 240, 130, 70, 120};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        load();
    }

    private JPanel heading() {
        JPanel box = new JPanel(new BorderLayout(12, 12)); box.setBackground(Color.WHITE);
        JPanel text = new JPanel(); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS)); text.setBackground(Color.WHITE);
        JLabel title = new JLabel("Members"); title.setFont(Main.TITLE);
        JLabel note = new JLabel("Keep the library member list accurate and searchable.");
        note.setFont(Main.BODY); note.setForeground(Color.GRAY);
        text.add(title); text.add(Box.createVerticalStrut(4)); text.add(note); box.add(text, BorderLayout.NORTH);
        JPanel actions = Main.card(); actions.setLayout(new FlowLayout(FlowLayout.LEFT, 9, 0));
        actions.add(new JLabel("Search")); actions.add(search);
        JButton find = Main.button("Search", false); find.addActionListener(e -> load()); actions.add(find);
        JButton all = Main.button("View All", false); all.addActionListener(e -> { search.setText(""); load(); }); actions.add(all);
        JButton add = Main.button("+ Add Member", true); add.addActionListener(e -> showAddForm()); actions.add(add);
        JButton delete = Main.button("Delete Selected", false); delete.addActionListener(e -> deleteMember()); actions.add(delete);
        search.addActionListener(e -> load()); box.add(actions, BorderLayout.SOUTH);
        return box;
    }

    private void showAddForm() {
        JTextField roll = Main.input(18), name = Main.input(18), email = Main.input(18);
        JTextField phone = Main.input(18), year = Main.input(18), branch = Main.input(18);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        addField(form, "Roll number", roll); addField(form, "Name", name); addField(form, "Email", email);
        addField(form, "Phone", phone); addField(form, "Year", year); addField(form, "Branch", branch);
        if (JOptionPane.showConfirmDialog(this, form, "Add Member", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            Database.addMember(roll.getText(), name.getText(), email.getText(), phone.getText(),
                    Main.number(year, "Year"), branch.getText());
            JOptionPane.showMessageDialog(this, "Member added."); load();
        } catch (Exception e) { Main.error(this, e); }
    }

    private void addField(JPanel panel, String name, JTextField field) {
        panel.add(new JLabel(name)); panel.add(field);
    }

    private void deleteMember() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a member first."); return; }
        String roll = model.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Delete member " + roll + "?", "Confirm delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { Database.deleteMember(roll); JOptionPane.showMessageDialog(this, "Member deleted."); load(); }
        catch (SQLException e) { Main.error(this, e); }
    }

    void load() {
        try { Database.loadMembers(model, search.getText().trim()); }
        catch (SQLException e) { Main.error(this, e); }
    }
}
