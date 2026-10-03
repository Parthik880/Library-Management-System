package frontend;

import backend.StudentDatabase;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class StudentMain {
    private static String currentRollNo;

    public static String getCurrentRollNo() {
        return currentRollNo;
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        SwingUtilities.invokeLater(StudentMain::open);
    }

    public static void open() {
        // ponytail: synchronous JDBC for a local mini-project; use SwingWorker if queries become slow.
        JFrame frame = new JFrame("Library Student Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        JPanel login = new JPanel(new GridBagLayout());
        JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
        JTextField roll = new JTextField(20);
        JLabel label = new JLabel("Roll No");
        label.setLabelFor(roll);
        JButton enter = new JButton("Open Student Portal");
        form.add(new JLabel("STUDENT PORTAL"));
        form.add(label);
        form.add(roll);
        form.add(enter);
        login.add(form);
        frame.add(login);
        enter.addActionListener(e -> {
            try {
                String value = roll.getText().trim();
                if (value.isEmpty() || !StudentDatabase.studentExists(value)) {
                    JOptionPane.showMessageDialog(frame, "Enter an existing student roll number.");
                    return;
                }
                currentRollNo = value;
                showPortal(frame);
            } catch (SQLException error) {
                showError(frame, error);
            }
        });
        roll.addActionListener(e -> enter.doClick());
        frame.setVisible(true);
    }

    private static void showPortal(JFrame frame) {
        CardLayout layout = new CardLayout();
        JPanel content = new JPanel(layout);
        StudentBooksPanel books = new StudentBooksPanel();
        MyBooksPanel myBooks = new MyBooksPanel();
        FinePanel fines = new FinePanel();
        String[] names = {"Books", "My Books", "Fines"};
        JPanel[] pages = {books, myBooks, fines};
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(new EmptyBorder(25, 18, 25, 18));
        sidebar.setBackground(new Color(23, 43, 77));
        JLabel title = new JLabel("STUDENT PORTAL");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sidebar.add(title);
        JLabel student = new JLabel(currentRollNo);
        student.setForeground(Color.WHITE);
        sidebar.add(student);
        sidebar.add(Box.createVerticalStrut(30));
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            content.add(pages[i], name);
            JButton button = new JButton(name);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBackground(new Color(23, 43, 77));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                layout.show(content, name);
                switch (name) {
                    case "Books": books.refresh(); break;
                    case "My Books": myBooks.refresh(); break;
                    case "Fines": fines.refresh(); break;
                }
            });
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(8));
        }
        frame.getContentPane().removeAll();
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(content, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
        books.refresh();
    }

    static JTable createTable(String[] columns, int... widths) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return table;
    }

    static JScrollPane scroll(Component content) {
        JScrollPane scroll = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    static void showDetails(Component parent, String title, String[] labels, String[] values) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            panel.add(label);
            String value = values[i];
            if (!labels[i].equals("Book Details") && !labels[i].equals("Issue Details")) {
                panel.add(new JLabel(value == null || value.isBlank() ? "-" : value));
            }
            panel.add(Box.createVerticalStrut(12));
        }
        JScrollPane scroll = scroll(panel);
        scroll.setPreferredSize(new Dimension(500, 500));
        JOptionPane.showMessageDialog(parent, scroll, title, JOptionPane.PLAIN_MESSAGE);
    }

    static void showError(Component parent, SQLException error) {
        JOptionPane.showMessageDialog(parent, error.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
