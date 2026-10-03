package frontend;

import backend.MembersDatabase;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MembersPanel extends JPanel {
    private final JTable table = Main.createTable(
            new String[]{"Name", "Roll No", "Issue Status"}, 320, 220, 180);
    private final DefaultTableModel model = (DefaultTableModel) table.getModel();
    private final JTextField searchField = new JTextField(25);

    public MembersPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(246, 248, 252));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Members");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        controls.setOpaque(false);
        JButton search = new JButton("Search");
        search.addActionListener(e -> refresh());
        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> { searchField.setText(""); refresh(); });
        JButton add = new JButton("+ Add Member");
        add.addActionListener(e -> showAddMemberDialog());
        controls.add(searchField);
        controls.add(search);
        controls.add(reset);
        controls.add(add);
        header.add(controls, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(Main.scroll(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton view = new JButton("View Details");
        view.addActionListener(e -> viewDetails());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> deleteMember());
        buttons.add(view);
        buttons.add(delete);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        try {
            List<MembersDatabase.MemberData> members =
                    MembersDatabase.searchMembers(searchField.getText().trim());
            model.setRowCount(0);
            for (MembersDatabase.MemberData member : members) {
                model.addRow(new Object[]{member.name(), member.rollNo(), member.status()});
            }
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void showAddMemberDialog() {
        String[] labels = {"Name", "Roll No", "Email", "Phone", "Year", "Branch"};
        JTextField[] fields = new JTextField[labels.length];
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new JTextField();
            form.add(new JLabel(labels[i] + ":"));
            form.add(fields[i]);
        }
        if (JOptionPane.showConfirmDialog(this, form, "Add Member", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        try {
            boolean added = MembersDatabase.addMember(fields[0].getText().trim(), fields[1].getText().trim(),
                    fields[2].getText().trim(), fields[3].getText().trim(),
                    Integer.parseInt(fields[4].getText().trim()), fields[5].getText().trim());
            JOptionPane.showMessageDialog(this, added ? "Member added successfully." : "Could not add member.");
            if (added) refresh();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Year must be a number.");
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a member first.");
            return;
        }
        try {
            MembersDatabase.MemberDetails member =
                    MembersDatabase.getMemberDetails(model.getValueAt(row, 1).toString());
            if (member == null) {
                JOptionPane.showMessageDialog(this, "Member not found.");
                return;
            }
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(20, 25, 20, 25));
            addHeading(panel, "Student Details");
            addDetail(panel, "Name", member.name());
            addDetail(panel, "Roll No", member.rollNo());
            addDetail(panel, "Email", member.email());
            addDetail(panel, "Phone", member.phone());
            addDetail(panel, "Year", String.valueOf(member.year()));
            addDetail(panel, "Branch", member.branch());
            panel.add(new JSeparator());
            addHeading(panel, "Book Details");
            addDetail(panel, "Book ID", member.bookId());
            addDetail(panel, "Book Name", member.bookName());
            addDetail(panel, "Issue Date", member.issueDate());
            addDetail(panel, "Due Date", member.dueDate());
            addDetail(panel, "Submission Date", member.submissionDate());
            addDetail(panel, "Status", member.status());
            addDetail(panel, "Fine", "₹" + member.fine());
            JScrollPane scroll = Main.scroll(panel);
            scroll.setPreferredSize(new Dimension(450, 550));
            JOptionPane.showMessageDialog(this, scroll, "Member Details", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }

    private void deleteMember() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a member first.");
            return;
        }
        String rollNo = model.getValueAt(row, 1).toString();
        if (JOptionPane.showConfirmDialog(this, "Delete member " + rollNo + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            boolean deleted = MembersDatabase.deleteMember(rollNo);
            JOptionPane.showMessageDialog(this, deleted ? "Member deleted." : "Member not found.");
            refresh();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete member. Members with transaction history must be kept.\n" + e.getMessage());
        }
    }

    private void addHeading(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(label);
        panel.add(Box.createVerticalStrut(15));
    }

    private void addDetail(JPanel panel, String label, String value) {
        JLabel heading = new JLabel(label);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(heading);
        panel.add(new JLabel(value == null || value.isBlank() ? "-" : value));
        panel.add(Box.createVerticalStrut(10));
    }
}
