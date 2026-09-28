package frontend;

import backend.BooksDatabase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BooksPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private static final Color BACKGROUND =
            new Color(246, 248, 252);

    private static final Color PRIMARY =
            new Color(37, 99, 235);

    private static final Color DANGER =
            new Color(220, 38, 38);


    public BooksPanel() {

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        setBorder(
                new EmptyBorder(
                        30,
                        35,
                        30,
                        35
                )
        );

        createHeader();
        createTable();

        loadBooks();
    }


    // =====================================================
    // HEADER
    // =====================================================

    private void createHeader() {

        JPanel topPanel =
                new JPanel(new BorderLayout());

        topPanel.setOpaque(false);


        JPanel titlePanel = new JPanel();

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );

        titlePanel.setOpaque(false);


        JLabel title =
                new JLabel("Books");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        30
                )
        );


        JLabel subtitle =
                new JLabel(
                        "Manage books and view current issues"
                );

        subtitle.setForeground(
                new Color(100, 116, 139)
        );


        titlePanel.add(title);
        titlePanel.add(
                Box.createVerticalStrut(4)
        );
        titlePanel.add(subtitle);


        topPanel.add(
                titlePanel,
                BorderLayout.NORTH
        );


        // ---------------------------
        // SEARCH / ADD
        // ---------------------------

        JPanel controls =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                15
                        )
                );

        controls.setOpaque(false);


        searchField =
                new JTextField(25);

        searchField.setPreferredSize(
                new Dimension(
                        280,
                        38
                )
        );


        JButton searchButton =
                new JButton("Search");

        JButton resetButton =
                new JButton("Reset");

        JButton addButton =
                new JButton("+ Add Book");


        stylePrimaryButton(addButton);
        stylePrimaryButton(searchButton);


        searchButton.addActionListener(
                e -> searchBooks()
        );

        resetButton.addActionListener(
                e -> {
                    searchField.setText("");
                    loadBooks();
                }
        );

        addButton.addActionListener(
                e -> showAddBookDialog()
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
                "Book ID",
                "Title",
                "Author",
                "Availability"
        };


        tableModel =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {

                        return false;
                    }
                };


        table =
                new JTable(tableModel);

        table.setRowHeight(42);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );


        JScrollPane scrollPane =
                new JScrollPane(table);


        JPanel centerPanel =
                new JPanel(
                        new BorderLayout()
                );

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


        // ---------------------------
        // BOTTOM ACTIONS
        // ---------------------------

        JPanel bottomButtons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                10
                        )
                );

        bottomButtons.setOpaque(false);


        JButton viewButton =
                new JButton(
                        "View Details"
                );

        JButton deleteButton =
                new JButton(
                        "Delete"
                );


        stylePrimaryButton(viewButton);

        deleteButton.setBackground(DANGER);
        deleteButton.setForeground(Color.WHITE);


        viewButton.addActionListener(
                e -> viewBookDetails()
        );

        deleteButton.addActionListener(
                e -> deleteBook()
        );


        bottomButtons.add(
                viewButton
        );

        bottomButtons.add(
                deleteButton
        );


        centerPanel.add(
                bottomButtons,
                BorderLayout.SOUTH
        );


        add(
                centerPanel,
                BorderLayout.CENTER
        );
    }


    // =====================================================
    // LOAD BOOKS
    // =====================================================

    private void loadBooks() {

        tableModel.setRowCount(0);

        List<BooksDatabase.BookData> books =
                BooksDatabase.getBooks();


        for (
                BooksDatabase.BookData book
                : books
        ) {

            tableModel.addRow(
                    new Object[]{
                            book.bookId(),
                            book.title(),
                            book.author(),
                            book.status()
                    }
            );
        }
    }


    // =====================================================
    // SEARCH
    // =====================================================

    private void searchBooks() {

        String keyword =
                searchField
                        .getText()
                        .trim();


        if (keyword.isEmpty()) {

            loadBooks();
            return;
        }


        tableModel.setRowCount(0);


        List<BooksDatabase.BookData> books =
                BooksDatabase.searchBooks(
                        keyword
                );


        for (
                BooksDatabase.BookData book
                : books
        ) {

            tableModel.addRow(
                    new Object[]{
                            book.bookId(),
                            book.title(),
                            book.author(),
                            book.status()
                    }
            );
        }
    }


    // =====================================================
    // ADD BOOK
    // =====================================================

    private void showAddBookDialog() {

        JTextField idField =
                new JTextField();

        JTextField titleField =
                new JTextField();

        JTextField authorField =
                new JTextField();

        JTextField isbnField =
                new JTextField();

        JTextField categoryField =
                new JTextField();

        JTextField copiesField =
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
                new JLabel("Book ID:")
        );

        panel.add(idField);


        panel.add(
                new JLabel("Title:")
        );

        panel.add(titleField);


        panel.add(
                new JLabel("Author:")
        );

        panel.add(authorField);


        panel.add(
                new JLabel("ISBN:")
        );

        panel.add(isbnField);


        panel.add(
                new JLabel("Category:")
        );

        panel.add(categoryField);


        panel.add(
                new JLabel("Total Copies:")
        );

        panel.add(copiesField);


        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Add Book",
                        JOptionPane.OK_CANCEL_OPTION
                );


        if (
                result
                != JOptionPane.OK_OPTION
        ) {

            return;
        }


        try {

            String bookId =
                    idField
                            .getText()
                            .trim();

            String title =
                    titleField
                            .getText()
                            .trim();

            String author =
                    authorField
                            .getText()
                            .trim();

            String isbn =
                    isbnField
                            .getText()
                            .trim();

            String category =
                    categoryField
                            .getText()
                            .trim();

            int copies =
                    Integer.parseInt(
                            copiesField
                                    .getText()
                                    .trim()
                    );


            if (
                    bookId.isEmpty()
                    || title.isEmpty()
                    || author.isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book ID, title and author are required."
                );

                return;
            }


            boolean success =
                    BooksDatabase.addBook(
                            bookId,
                            title,
                            author,
                            isbn,
                            category,
                            copies
                    );


            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book added successfully."
                );

                loadBooks();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Could not add book."
                );
            }


        } catch (
                NumberFormatException e
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Total copies must be a number."
            );
        }
    }


    // =====================================================
    // VIEW DETAILS
    // =====================================================

    private void viewBookDetails() {

        int row =
                table.getSelectedRow();


        if (row == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select a book first."
            );

            return;
        }


        String bookId =
                tableModel
                        .getValueAt(
                                row,
                                0
                        )
                        .toString();


        BooksDatabase.BookDetails book =
                BooksDatabase.getBookDetails(
                        bookId
                );


        if (book == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Book not found."
            );

            return;
        }


        // ---------------------------
        // MAIN DIALOG
        // ---------------------------

        JDialog dialog =
                new JDialog(
                        SwingUtilities
                                .getWindowAncestor(this),
                        "Book Details",
                        Dialog.ModalityType.APPLICATION_MODAL
                );


        dialog.setSize(
                850,
                650
        );

        dialog.setLocationRelativeTo(
                this
        );


        JPanel mainPanel =
                new JPanel();

        mainPanel.setLayout(
                new BoxLayout(
                        mainPanel,
                        BoxLayout.Y_AXIS
                )
        );

        mainPanel.setBorder(
                new EmptyBorder(
                        25,
                        30,
                        25,
                        30
                )
        );


        // ---------------------------
        // BOOK TITLE
        // ---------------------------

        JLabel title =
                new JLabel(
                        book.title()
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        26
                )
        );


        JLabel author =
                new JLabel(
                        book.author()
                );

        author.setForeground(
                new Color(
                        100,
                        116,
                        139
                )
        );


        mainPanel.add(title);

        mainPanel.add(
                Box.createVerticalStrut(4)
        );

        mainPanel.add(author);

        mainPanel.add(
                Box.createVerticalStrut(20)
        );


        // ---------------------------
        // BOOK INFORMATION
        // ---------------------------

        JLabel detailsHeading =
                new JLabel(
                        "BOOK DETAILS"
                );

        detailsHeading.setForeground(
                PRIMARY
        );

        detailsHeading.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );


        mainPanel.add(
                detailsHeading
        );

        mainPanel.add(
                Box.createVerticalStrut(10)
        );


        JPanel detailsGrid =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                20,
                                10
                        )
                );

        detailsGrid.setOpaque(false);


        detailsGrid.add(
                detailPanel(
                        "Book ID",
                        book.bookId()
                )
        );

        detailsGrid.add(
                detailPanel(
                        "ISBN",
                        book.isbn()
                )
        );

        detailsGrid.add(
                detailPanel(
                        "Category",
                        book.category()
                )
        );

        detailsGrid.add(
                detailPanel(
                        "Status",
                        book.status()
                )
        );


        mainPanel.add(
                detailsGrid
        );


        mainPanel.add(
                Box.createVerticalStrut(20)
        );

        mainPanel.add(
                new JSeparator()
        );

        mainPanel.add(
                Box.createVerticalStrut(20)
        );


        // ---------------------------
        // STOCK
        // ---------------------------

        JLabel stockHeading =
                new JLabel("STOCK");

        stockHeading.setForeground(
                PRIMARY
        );

        stockHeading.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );


        mainPanel.add(stockHeading);

        mainPanel.add(
                Box.createVerticalStrut(10)
        );


        JPanel stockPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                15,
                                0
                        )
                );

        stockPanel.setOpaque(false);


        stockPanel.add(
                stockCard(
                        "Total Copies",
                        book.totalCopies()
                )
        );

        stockPanel.add(
                stockCard(
                        "Available",
                        book.availableCopies()
                )
        );

        stockPanel.add(
                stockCard(
                        "Issued",
                        book.issuedCopies()
                )
        );


        mainPanel.add(stockPanel);


        mainPanel.add(
                Box.createVerticalStrut(25)
        );

        mainPanel.add(
                new JSeparator()
        );

        mainPanel.add(
                Box.createVerticalStrut(20)
        );


        // ---------------------------
        // CURRENTLY ISSUED TO
        // ---------------------------

        JLabel issuedHeading =
                new JLabel(
                        "CURRENTLY ISSUED TO"
                );

        issuedHeading.setForeground(
                PRIMARY
        );

        issuedHeading.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );


        mainPanel.add(
                issuedHeading
        );

        mainPanel.add(
                Box.createVerticalStrut(10)
        );


        String[] columns = {
                "Roll No",
                "Name",
                "Issue Date",
                "Due Date",
                "Status",
                "Fine"
        };


        DefaultTableModel issuedModel =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {

                        return false;
                    }
                };


        List<BooksDatabase.IssuedStudent> students =
                BooksDatabase.getIssuedStudents(
                        bookId
                );


        for (
                BooksDatabase.IssuedStudent student
                : students
        ) {

            issuedModel.addRow(
                    new Object[]{
                            student.rollNo(),
                            student.name(),
                            student.issueDate(),
                            student.dueDate(),
                            student.status(),
                            "₹" + student.fine()
                    }
            );
        }


        JTable issuedTable =
                new JTable(
                        issuedModel
                );

        issuedTable.setRowHeight(30);


        JScrollPane issuedScroll =
                new JScrollPane(
                        issuedTable
                );

        issuedScroll.setPreferredSize(
                new Dimension(
                        750,
                        180
                )
        );


        mainPanel.add(
                issuedScroll
        );


        JScrollPane outerScroll =
                new JScrollPane(
                        mainPanel
                );

        outerScroll.setBorder(null);


        dialog.add(
                outerScroll
        );

        dialog.setVisible(true);
    }


    // =====================================================
    // DELETE BOOK
    // =====================================================

    private void deleteBook() {

        int row =
                table.getSelectedRow();


        if (row == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Select a book first."
            );

            return;
        }


        String bookId =
                tableModel
                        .getValueAt(
                                row,
                                0
                        )
                        .toString();


        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete book "
                                + bookId
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
                BooksDatabase.deleteBook(
                        bookId
                );


        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Book deleted."
            );

            loadBooks();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not delete book.\n"
                            + "It may have transaction history."
            );
        }
    }


    // =====================================================
    // DETAIL PANEL
    // =====================================================

    private JPanel detailPanel(
            String title,
            String value
    ) {

        JPanel panel =
                new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setOpaque(false);


        JLabel heading =
                new JLabel(title);

        heading.setForeground(
                new Color(
                        100,
                        116,
                        139
                )
        );

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

        panel.add(
                Box.createVerticalStrut(3)
        );

        panel.add(data);


        return panel;
    }


    // =====================================================
    // STOCK CARD
    // =====================================================

    private JPanel stockCard(
            String title,
            int value
    ) {

        JPanel card =
                new JPanel();

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                new EmptyBorder(
                        15,
                        18,
                        15,
                        18
                )
        );

        card.setBackground(
                new Color(
                        248,
                        250,
                        252
                )
        );


        JLabel heading =
                new JLabel(title);

        heading.setForeground(
                new Color(
                        100,
                        116,
                        139
                )
        );


        JLabel number =
                new JLabel(
                        String.valueOf(
                                value
                        )
                );

        number.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );


        card.add(heading);

        card.add(
                Box.createVerticalStrut(5)
        );

        card.add(number);


        return card;
    }


    // =====================================================
    // BUTTON STYLE
    // =====================================================

    private void stylePrimaryButton(
            JButton button
    ) {

        button.setBackground(
                PRIMARY
        );

        button.setForeground(
                Color.WHITE
        );

        button.setFocusPainted(
                false
        );
    }
}