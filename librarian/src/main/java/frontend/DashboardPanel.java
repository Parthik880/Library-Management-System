package frontend;

import backend.TransactionDatabase;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class DashboardPanel extends JPanel {
    private final JLabel[] values = new JLabel[6];

    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 35, 30, 35));
        setBackground(new Color(246, 248, 252));
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        add(title, BorderLayout.NORTH);
        String[] names = {"Total Members", "Total Books", "Available Books",
                "Issued Books", "Late Books", "Total Outstanding Fine"};
        JPanel cards = new JPanel(new GridLayout(2, 3, 20, 20));
        cards.setOpaque(false);
        for (int i = 0; i < names.length; i++) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBorder(new EmptyBorder(25, 25, 25, 25));
            card.add(new JLabel(names[i]), BorderLayout.NORTH);
            values[i] = new JLabel("-");
            values[i].setFont(new Font("Segoe UI", Font.BOLD, 30));
            card.add(values[i], BorderLayout.CENTER);
            cards.add(card);
        }
        add(cards, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        footer.add(refresh);
        footer.add(new JLabel("Books = titles; available/issued = copies. Fine includes recorded and active fines."));
        add(footer, BorderLayout.SOUTH);
    }

    public void refresh() {
        try {
            double[] counts = TransactionDatabase.getDashboardCounts();
            for (int i = 0; i < values.length; i++) {
                values[i].setText(i == 5 ? String.format("₹%.2f", counts[i]) : String.valueOf((int) counts[i]));
            }
        } catch (SQLException e) {
            Main.showError(this, e);
        }
    }
}
