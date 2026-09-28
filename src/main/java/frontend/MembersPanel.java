package frontend;

import backend.MembersDatabase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MembersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public MembersPanel() {

        setLayout(new BorderLayout());
        setBackground(new Color(246, 248, 252));
        setBorder(new EmptyBorder(30, 35, 30, 35));

        createHeader();
        createTable();

        loadMembers();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private void createHeader() {

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Members");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));

        topPanel.add(title, BorderLayout.NORTH);


        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 15)
        );

        controls.setOpaque(false);


        searchField = new JTextField(25);

        searchField.setPreferredSize(
                new Dimension(280, 38)
        );


        JButton searchButton =
                new JButton("Search");

        JButton resetButton =
                new JButton("Reset");

        JButton addButton =
                new JButton("+ Add Member");


        searchButton.addActionListener(
                e -> searchMembers()
        );

        resetButton.addActionListener(
                e -> {
                    searchField.setText("");
                    loadMembers();
                }
        );

        addButton.addActionListener(
                e -> showAddMemberDialog()
        );


        controls.add(searchField);
        controls.add(searchButton);
        controls.add(resetButton);
        controls.add(addButton);


        topPanel.add(
                controls,
                BorderLayout.SOUTH
        );

        add(
                topPanel,
                BorderLayout.NORTH
        );
    }


    // =====================================================
    // TABLE
    // =====================================================

    private void createTable() {

        String[] columns = {
                "Name",
                "Roll No",
                "Issue Status"
        };


        tableModel = new DefaultTableModel(
                columns,
                0
        ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };


        table = new JTable(tableModel);

        table.setRowHeight(40);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );


        JScrollPane scrollPane =
                new JScrollPane(table);
        table.setAutoResizeMode(
        JTable.AUTO_RESIZE_OFF
        );

        table.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(320);

        table.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(220);

        table.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(180);


        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);


        JPanel centerPanel =
                new JPanel(new BorderLayout());

        centerPanel.setOpaque(false);

        centerPanel.setBorder(
                new EmptyBorder(
                        10,
                        0,
                        0,
                        0
                )
        );


        centerPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        // -----------------------------
        // BOTTOM BUTTONS
        // -----------------------------

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        buttons.setOpaque(false);


        JButton viewButton =
                new JButton("View Details");

        JButton deleteButton =
                new JButton("Delete");


        viewButton.addActionListener(
                e -> viewDetails()
        );

        deleteButton.addActionListener(
                e -> deleteMember()
        );


        buttons.add(viewButton);
        buttons.add(deleteButton);


        centerPanel.add(
                buttons,
                BorderLayout.SOUTH
        );


        add(
                centerPanel,
                BorderLayout.CENTER
        );
    }


    // =====================================================
    // LOAD FROM DATABASE
    // =====================================================

    private void loadMembers() {

        tableModel.setRowCount(0);

        List<MembersDatabase.MemberData> members =
                MembersDatabase.getMembers();


        for (
                MembersDatabase.MemberData member
                : members
        ) {

            tableModel.addRow(
                    new Object[]{
                            member.name(),
                            member.rollNo(),
                            member.status()
                    }
            );
        }
    }


    // =====================================================
    // SEARCH
    // =====================================================

    private void searchMembers() {

        String keyword =
                searchField
                        .getText()
                        .trim()
                        .toLowerCase();


        tableModel.setRowCount(0);


        List<MembersDatabase.MemberData> members =
                MembersDatabase.getMembers();


        for (
                MembersDatabase.MemberData member
                : members
        ) {

            if (
                    member.name()
                            .toLowerCase()
                            .contains(keyword)

                    ||

                    member.rollNo()
                            .toLowerCase()
                            .contains(keyword)
            ) {

                tableModel.addRow(
                        new Object[]{
                                member.name(),
                                member.rollNo(),
                                member.status()
                        }
                );
            }
        }
    }


    // =====================================================
    // ADD MEMBER
    // =====================================================

    private void showAddMemberDialog() {

        JTextField nameField =
                new JTextField();

        JTextField rollField =
                new JTextField();

        JTextField emailField =
                new JTextField();

        JTextField phoneField =
                new JTextField();

        JTextField yearField =
                new JTextField();

        JTextField branchField =
                new JTextField();


        JPanel panel =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                10,
                                10
                        )
                );


        panel.add(
                new JLabel("Name:")
        );

        panel.add(nameField);


        panel.add(
                new JLabel("Roll No:")
        );

        panel.add(rollField);


        panel.add(
                new JLabel("Email:")
        );

        panel.add(emailField);


        panel.add(
                new JLabel("Phone:")
        );

        panel.add(phoneField);


        panel.add(
                new JLabel("Year:")
        );

        panel.add(yearField);


        panel.add(
                new JLabel("Branch:")
        );

        panel.add(branchField);


        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Add Member",
                        JOptionPane.OK_CANCEL_OPTION
                );


        if (
                result
                != JOptionPane.OK_OPTION
        ) {

            return;
        }


        try {

            String name =
                    nameField
                            .getText()
                            .trim();

            String rollNo =
                    rollField
                            .getText()
                            .trim();

            String email =
                    emailField
                            .getText()
                            .trim();

            String phone =
                    phoneField
                            .getText()
                            .trim();

            int year =
                    Integer.parseInt(
                            yearField
                                    .getText()
                                    .trim()
                    );

            String branch =
                    branchField
                            .getText()
                            .trim();


            if (
                    name.isEmpty()
                    || rollNo.isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "Name and Roll No are required."
                );

                return;
            }


            boolean success =
                    MembersDatabase.addMember(
                            name,
                            rollNo,
                            email,
                            phone,
                            year,
                            branch
                    );


            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Member added successfully."
                );

                loadMembers();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Could not add member."
                );
            }


        } catch (
                NumberFormatException e
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Year must be a number."
            );
        }
    }


    // =====================================================
    // VIEW DETAILS
    // =====================================================

    private void viewDetails() {

        int row =
                table.getSelectedRow();


        if (row == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select a member first."
            );

            return;
        }


        String rollNo =
                tableModel
                        .getValueAt(
                                row,
                                1
                        )
                        .toString();


        MembersDatabase.MemberDetails member =
                MembersDatabase.getMemberDetails(
                        rollNo
                );


        if (member == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Member not found."
            );

            return;
        }


        JPanel panel =
                new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setBorder(
                new EmptyBorder(
                        20,
                        25,
                        20,
                        25
                )
        );


        JLabel title =
                new JLabel(
                        "Student Details"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        20
                )
        );


        panel.add(title);

        panel.add(
                Box.createVerticalStrut(15)
        );


        addDetail(
                panel,
                "Name",
                member.name()
        );

        addDetail(
                panel,
                "Roll No",
                member.rollNo()
        );

        addDetail(
                panel,
                "Email",
                member.email()
        );

        addDetail(
                panel,
                "Phone",
                member.phone()
        );

        addDetail(
                panel,
                "Year",
                String.valueOf(
                        member.year()
                )
        );

        addDetail(
                panel,
                "Branch",
                member.branch()
        );


        panel.add(
                new JSeparator()
        );

        panel.add(
                Box.createVerticalStrut(15)
        );


        JLabel bookTitle =
                new JLabel(
                        "Book Details"
                );

        bookTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18
                )
        );


        panel.add(bookTitle);

        panel.add(
                Box.createVerticalStrut(10)
        );


        addDetail(
                panel,
                "Book ID",
                member.bookId()
        );

        addDetail(
                panel,
                "Book Name",
                member.bookName()
        );

        addDetail(
                panel,
                "Issue Date",
                member.issueDate()
        );

        addDetail(
                panel,
                "Due Date",
                member.dueDate()
        );

        addDetail(
                panel,
                "Submission Date",
                member.submissionDate()
        );

        addDetail(
                panel,
                "Status",
                member.status()
        );

        addDetail(
                panel,
                "Fine",
                "₹" + member.fine()
        );


        JScrollPane scroll =
                new JScrollPane(panel);

        scroll.setPreferredSize(
                new Dimension(
                        450,
                        550
                )
        );

        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scroll.getVerticalScrollBar()
                .setUnitIncrement(16);

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Member Details",
                JOptionPane.PLAIN_MESSAGE
        );
    }


    // =====================================================
    // DELETE MEMBER
    // =====================================================

    private void deleteMember() {

        int row =
                table.getSelectedRow();


        if (row == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select a member first."
            );

            return;
        }


        String rollNo =
                tableModel
                        .getValueAt(
                                row,
                                1
                        )
                        .toString();


        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete member "
                                + rollNo
                                + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );


        if (
                confirm
                != JOptionPane.YES_OPTION
        ) {

            return;
        }


        boolean success =
                MembersDatabase.deleteMember(
                        rollNo
                );


        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Member deleted."
            );

            loadMembers();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not delete member.\n" +
                    "The member may have transaction history."
            );
        }
    }


    // =====================================================
    // DETAIL LABEL
    // =====================================================

    private void addDetail(
            JPanel panel,
            String label,
            String value) {

        JLabel heading =
                new JLabel(label);

        heading.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );


        JLabel data =
                new JLabel(
                        value == null
                                || value.isBlank()
                                ? "-"
                                : value
                );


        panel.add(heading);

        panel.add(data);

        panel.add(
                Box.createVerticalStrut(
                        10
                )
        );
    }
}
