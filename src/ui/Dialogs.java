package ui;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

//Utility class for showing standard dialog messages to the user.
public final class Dialogs {

    private Dialogs() {
    }

    public static void info(JComponent parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg == null ? "" : msg, title == null ? "Info" : title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(JComponent parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg == null ? "" : msg, title == null ? "Error" : title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(JComponent parent, String title, String msg) {
        int r = JOptionPane.showConfirmDialog(parent, msg == null ? "" : msg, title == null ? "Confirm" : title, JOptionPane.YES_NO_OPTION);
        return r == JOptionPane.YES_OPTION;
    }

    public static String prompt(JComponent parent, String title, String msg, String initialValue) {
        Object r = JOptionPane.showInputDialog(parent, msg == null ? "" : msg, title == null ? "Input" : title,
                JOptionPane.QUESTION_MESSAGE, null, null, initialValue == null ? "" : initialValue);
        return (r == null) ? null : r.toString();
    }
}
