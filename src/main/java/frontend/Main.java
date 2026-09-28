package frontend;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Main {

    private static final Color SIDEBAR =
            new Color(23, 43, 77);

    private static final Color SIDEBAR_HOVER =
            new Color(35, 61, 104);

    private static final Color PRIMARY =
            new Color(37, 99, 235);

    private static final Color WHITE =
            Color.WHITE;

    public static void main(String[] args) {

        FlatLightLaf.setup();

        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);

        SwingUtilities.invokeLater(() -> {

            JFrame frame =
                    new JFrame("Library Management System");

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setSize(1280, 760);
            frame.setMinimumSize(
                    new Dimension(1100, 650)
            );

            frame.setLocationRelativeTo(null);

            frame.setLayout(
                    new BorderLayout()
            );


            // ===========================
            // SIDEBAR
            // ===========================

            JPanel sidebar =
                    new JPanel();

            sidebar.setPreferredSize(
                    new Dimension(220, 0)
            );

            sidebar.setBackground(SIDEBAR);

            sidebar.setLayout(
                    new BoxLayout(
                            sidebar,
                            BoxLayout.Y_AXIS
                    )
            );

            sidebar.setBorder(
                    new EmptyBorder(
                            25,
                            18,
                            25,
                            18
                    )
            );


            JLabel logo =
                    new JLabel("LIBRARY");

            logo.setForeground(WHITE);

            logo.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            24
                    )
            );

            logo.setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );


            JLabel subtitle =
                    new JLabel("Librarian Portal");

            subtitle.setForeground(
                    new Color(
                            180,
                            195,
                            220
                    )
            );

            subtitle.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            13
                    )
            );

            subtitle.setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );


            sidebar.add(logo);

            sidebar.add(
                    Box.createVerticalStrut(4)
            );

            sidebar.add(subtitle);

            sidebar.add(
                    Box.createVerticalStrut(35)
            );


            JButton dashboardButton =
                    createSidebarButton(
                            "Dashboard"
                    );

            JButton membersButton =
                    createSidebarButton(
                            "Members"
                    );

            JButton booksButton =
                    createSidebarButton(
                            "Books"
                    );

            JButton issueButton =
                    createSidebarButton(
                            "Issue / Return"
                    );

            JButton historyButton =
                    createSidebarButton(
                            "Transaction History"
                    );


            sidebar.add(dashboardButton);

            sidebar.add(
                    Box.createVerticalStrut(8)
            );

            sidebar.add(membersButton);

            sidebar.add(
                    Box.createVerticalStrut(8)
            );

            sidebar.add(booksButton);

            sidebar.add(
                    Box.createVerticalStrut(8)
            );

            sidebar.add(issueButton);

            sidebar.add(
                    Box.createVerticalStrut(8)
            );

            sidebar.add(historyButton);


            sidebar.add(
                    Box.createVerticalGlue()
            );


            JLabel librarian =
                    new JLabel("Librarian");

            librarian.setForeground(
                    new Color(
                            200,
                            210,
                            230
                    )
            );

            librarian.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            13
                    )
            );

            librarian.setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );


            sidebar.add(librarian);


            // ===========================
            // CARD LAYOUT
            // ===========================

            CardLayout cardLayout =
                    new CardLayout();

            JPanel contentPanel =
                    new JPanel(cardLayout);


            JPanel dashboard =
                    createPlaceholderPanel(
                            "Dashboard"
                    );

            MembersPanel members =
                    new MembersPanel();

            JPanel books =
                    new BooksPanel();
                    ;

            JPanel issue =
                    createPlaceholderPanel(
                            "Issue / Return"
                    );

            JPanel history =
                    createPlaceholderPanel(
                            "Transaction History"
                    );


            contentPanel.add(
                    dashboard,
                    "dashboard"
            );

            contentPanel.add(
                    members,
                    "members"
            );

            contentPanel.add(
                    books,
                    "books"
            );

            contentPanel.add(
                    issue,
                    "issue"
            );

            contentPanel.add(
                    history,
                    "history"
            );


            // ===========================
            // NAVIGATION
            // ===========================

            dashboardButton.addActionListener(
                    e ->
                            cardLayout.show(
                                    contentPanel,
                                    "dashboard"
                            )
            );

            membersButton.addActionListener(
                    e ->
                            cardLayout.show(
                                    contentPanel,
                                    "members"
                            )
            );

            booksButton.addActionListener(
                    e ->
                            cardLayout.show(
                                    contentPanel,
                                    "books"
                            )
            );

            issueButton.addActionListener(
                    e ->
                            cardLayout.show(
                                    contentPanel,
                                    "issue"
                            )
            );

            historyButton.addActionListener(
                    e ->
                            cardLayout.show(
                                    contentPanel,
                                    "history"
                            )
            );


            frame.add(
                    sidebar,
                    BorderLayout.WEST
            );

            frame.add(
                    contentPanel,
                    BorderLayout.CENTER
            );


            // Start on Members for now

            cardLayout.show(
                    contentPanel,
                    "members"
            );


            frame.setVisible(true);
        });
    }


    private static JButton createSidebarButton(
            String text) {

        JButton button =
                new JButton(text);

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        44
                )
        );

        button.setPreferredSize(
                new Dimension(
                        180,
                        44
                )
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setForeground(
                Color.WHITE
        );

        button.setBackground(
                SIDEBAR
        );

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        button.setFocusPainted(false);

        button.setBorder(
                new EmptyBorder(
                        10,
                        14,
                        10,
                        14
                )
        );

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );


        button.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {

                        button.setBackground(
                                SIDEBAR_HOVER
                        );
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {

                        button.setBackground(
                                SIDEBAR
                        );
                    }
                }
        );


        return button;
    }


    private static JPanel createPlaceholderPanel(
            String title) {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(
                new Color(
                        246,
                        248,
                        252
                )
        );

        JLabel label =
                new JLabel(title);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        30
                )
        );

        label.setBorder(
                new EmptyBorder(
                        30,
                        35,
                        30,
                        35
                )
        );

        panel.add(
                label,
                BorderLayout.NORTH
        );

        return panel;
    }
}