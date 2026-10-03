import frontend.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UiCheck {
    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
    }

    private static List<Component> contents(Container parent) {
        List<Component> result = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            result.add(child);
            if (child instanceof Container) result.addAll(contents((Container) child));
        }
        return result;
    }

    private static JButton button(Container parent, String text) {
        for (Component child : contents(parent)) {
            if (child instanceof JButton && ((JButton) child).getText().equals(text)) return (JButton) child;
        }
        throw new AssertionError("Button missing: " + text);
    }

    private static JPanel page(JFrame frame, Class<?> type) {
        for (Component child : contents(frame)) {
            if (type.isInstance(child)) return (JPanel) child;
        }
        throw new AssertionError("Page missing: " + type);
    }

    private static JTable table(Container parent) {
        for (Component child : contents(parent)) if (child instanceof JTable) return (JTable) child;
        throw new AssertionError("Table missing.");
    }

    private static List<JTextField> fields(Container parent) {
        List<JTextField> result = new ArrayList<>();
        for (Component child : contents(parent)) if (child instanceof JTextField) result.add((JTextField) child);
        return result;
    }

    private static boolean label(Container parent, String text) {
        for (Component child : contents(parent)) {
            if (child instanceof JLabel && ((JLabel) child).getText().contains(text)) return true;
        }
        return false;
    }

    private static JFrame frame(String title) {
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof JFrame && frame.getTitle().equals(title) && frame.isVisible()) return (JFrame) frame;
        }
        throw new AssertionError("Window missing: " + title);
    }

    // Exercise Swing components inside this test JVM; no desktop mouse/keyboard automation.
    private static void dialog(JButton trigger, String expectedText, int expectedRows) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        boolean[] observed = {false};
        Timer close = new Timer(100, event -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog && window.isVisible()) {
                    observed[0] = true;
                    try {
                        check(label((JDialog) window, expectedText), "Dialog text: " + expectedText);
                        if (expectedRows >= 0) check(table((JDialog) window).getRowCount() == expectedRows, "Dialog borrower rows");
                        scrollChecks((JDialog) window);
                    } catch (Throwable error) {
                        failure.set(error);
                    } finally {
                        window.dispose();
                    }
                }
            }
        });
        close.start();
        trigger.doClick();
        close.stop();
        check(observed[0], "Dialog must open.");
        if (failure.get() != null) throw new AssertionError(failure.get());
    }

    private static void scrollChecks(Container parent) {
        for (Component child : contents(parent)) {
            if (child instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) child;
                check(scroll.getVerticalScrollBarPolicy() == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, "Vertical policy");
                check(scroll.getHorizontalScrollBarPolicy() == JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, "Horizontal policy");
                check(scroll.getVerticalScrollBar().getUnitIncrement() == 16, "Scroll increment");
                if (scroll.isShowing()) {
                    Dimension view = scroll.getViewport().getViewSize();
                    Dimension extent = scroll.getViewport().getExtentSize();
                    if (view.height > extent.height) {
                        check(scroll.getVerticalScrollBar().isVisible(), "Vertical scrollbar visible on overflow");
                        scroll.getVerticalScrollBar().setValue(16);
                        check(scroll.getViewport().getViewPosition().y > 0, "Vertical scroll moves content");
                    }
                    if (view.width > extent.width) {
                        check(scroll.getHorizontalScrollBar().isVisible(), "Horizontal scrollbar visible on overflow");
                        scroll.getHorizontalScrollBar().setValue(16);
                        check(scroll.getViewport().getViewPosition().x > 0, "Horizontal scroll moves content");
                    }
                }
            }
            if (child instanceof JTable) {
                check(((JTable) child).getAutoResizeMode() == JTable.AUTO_RESIZE_OFF, "Horizontal table sizing");
            }
        }
    }

    private static void changeSettings(JPanel books, int newTotal, boolean rejected) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        int[] dialogs = {0};
        Timer save = new Timer(100, event -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog && window.isVisible()) {
                    JDialog dialog = (JDialog) window;
                    try {
                        if (dialog.getTitle().equals("Change Book Settings")) {
                            check(label(dialog, "Currently Issued:"), "Inventory shows active count");
                            check(label(dialog, "Available Copies:"), "Inventory shows availability");
                            int spinners = 0;
                            for (Component child : contents(dialog)) {
                                if (child instanceof JSpinner) {
                                    ((JSpinner) child).setValue(newTotal);
                                    spinners++;
                                }
                            }
                            check(spinners == 1, "Only total copies is editable");
                            dialogs[0]++;
                            button(dialog, "Save Changes").doClick();
                        } else {
                            check(rejected && label(dialog, "Cannot reduce total copies"), "Inventory validation dialog");
                            dialogs[0]++;
                            dialog.dispose();
                        }
                    } catch (Throwable error) {
                        failure.set(error);
                        dialog.dispose();
                    }
                }
            }
        });
        save.start();
        button(books, "Change Book Settings").doClick();
        save.stop();
        if (failure.get() != null) throw new AssertionError(failure.get());
        check(dialogs[0] == (rejected ? 2 : 1), "Inventory settings and result dialogs");
    }

    public static void main(String[] args) throws Exception {
        String url = System.getenv("LIBRARY_DB_URL");
        check(url != null && url.matches(".*currentSchema=library_check_[a-f0-9]{32}.*"),
                "UI checks require an isolated test schema.");
        com.formdev.flatlaf.FlatLightLaf.setup();
        String schema = args[0];
        check(schema.matches("library_check_[a-f0-9]{32}") && url.contains("currentSchema=" + schema),
                "Test schema name must match the connection.");
        LibraryCheck.execute("CREATE SCHEMA " + schema);
        try {
            for (String sql : java.nio.file.Files.readString(java.nio.file.Path.of(args[1])).split(";")) {
                if (!sql.isBlank()) LibraryCheck.execute(sql);
            }
            SwingUtilities.invokeAndWait(() -> {
                Main.open();
                JFrame librarian = frame("Library Management System");
                check(label(librarian, "Dashboard"), "Dashboard opens");
                button(librarian, "Members").doClick();
                JPanel members = page(librarian, MembersPanel.class);
                check(table(members).getRowCount() == 30, "Members load");
                fields(members).get(0).setText("Aarav");
                button(members, "Search").doClick();
                check(table(members).getRowCount() == 1, "Member search");
                table(members).setRowSelectionInterval(0, 0);
                dialog(button(members, "View Details"), "Student Details", -1);
                button(members, "Reset").doClick();
                check(table(members).getRowCount() == 30, "Member reset");

                button(librarian, "Books").doClick();
                JPanel books = page(librarian, BooksPanel.class);
                check(table(books).getRowCount() == 30, "Books load");
                fields(books).get(0).setText("Clean Code");
                button(books, "Search").doClick();
                check(table(books).getRowCount() == 1, "Book search");
                table(books).setRowSelectionInterval(0, 0);
                dialog(button(books, "View Details"), "Currently Issued To", 2);
                changeSettings(books, 1, true);
                check(table(books).getValueAt(0, 3).equals("Available"), "Invalid inventory leaves table unchanged");
                changeSettings(books, 2, false);
                int updatedRow = -1;
                for (int i = 0; i < table(books).getRowCount(); i++) {
                    if (table(books).getValueAt(i, 0).equals("B001")) {
                        check(table(books).getValueAt(i, 3).equals("Unavailable"), "Books refreshes after settings save");
                        table(books).setRowSelectionInterval(i, i);
                        updatedRow = i;
                    }
                }
                check(updatedRow >= 0, "Updated book stays in the table");
                changeSettings(books, 8, false);
                check(table(books).getValueAt(updatedRow, 3).equals("Available"), "Settings increase refreshes availability");
                button(books, "Reset").doClick();

                button(librarian, "Issue / Return").doClick();
                JPanel issue = page(librarian, IssueReturnPanel.class);
                fields(issue).get(0).setText("ME2022030");
                fields(issue).get(1).setText("B030");
                dialog(button(issue, "Issue Book"), "Book issued for 14 days.", -1);
                check(table(issue).getRowCount() == 1, "Issue UI shows active row");
                dialog(button(issue, "Return Book"), "Book returned.", -1);
                check(table(issue).getRowCount() == 0, "Return UI clears active row");

                button(librarian, "Transaction History").doClick();
                JPanel history = page(librarian, HistoryPanel.class);
                fields(history).get(0).setText("B030");
                button(history, "Search").doClick();
                check(table(history).getRowCount() == 1, "History search includes returned transaction");
                check(table(history).getValueAt(0, 8).equals("Submitted"), "History submitted status");
                scrollChecks(librarian);

                StudentMain.open();
                JFrame student = frame("Library Student Portal");
                fields(student).get(0).setText("missing");
                dialog(button(student, "Open Student Portal"), "Enter an existing student roll number.", -1);
                fields(student).get(0).setText("CSE2024002");
                button(student, "Open Student Portal").doClick();
                check(StudentMain.getCurrentRollNo().equals("CSE2024002"), "Valid login stores roll");
                JPanel catalogue = page(student, StudentBooksPanel.class);
                check(table(catalogue).getRowCount() == 30, "Student catalogue loads");
                fields(catalogue).get(0).setText("Clean Code");
                button(catalogue, "Search").doClick();
                check(table(catalogue).getRowCount() == 1, "Student catalogue search");
                check(table(catalogue).getValueAt(0, 5).equals("Late"), "Live student overdue status");
                check(table(catalogue).getValueAt(0, 6).equals("₹30.0"), "Live student overdue fine");
                table(catalogue).setRowSelectionInterval(0, 0);
                dialog(button(catalogue, "View Details"), "Issue Date", -1);
                button(student, "My Books").doClick();
                JPanel myBooks = page(student, MyBooksPanel.class);
                check(table(myBooks).getRowCount() == 1, "Student personal history");
                table(myBooks).setRowSelectionInterval(0, 0);
                dialog(button(myBooks, "View Details"), "Issue Details", -1);
                button(student, "Fines").doClick();
                JPanel fines = page(student, FinePanel.class);
                check(label(fines, "Total Fine: ₹30.00"), "Student fine total");
                check(table(fines).getRowCount() == 1, "Student fine rows");
                scrollChecks(student);
                System.out.println("PASS: librarian/student navigation, search, login, details, inventory settings, issue/return UI, history, fines and scrolling.");
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                for (Window window : Window.getWindows()) window.dispose();
            });
            LibraryCheck.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }
}
