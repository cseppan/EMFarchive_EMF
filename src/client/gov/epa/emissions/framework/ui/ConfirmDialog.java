package gov.epa.emissions.framework.ui;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class ConfirmDialog {

    private String message;

    private String title;

    private JComponent parentWindow;

    public ConfirmDialog(String message, String title, JComponent parentWindow) {
        this.message = message;
        this.title = title;
        this.parentWindow = parentWindow;

    }

    public boolean confirm() {
        int option = JOptionPane.showConfirmDialog(parentWindow, message, title, JOptionPane.YES_NO_OPTION);
        return (option == 0);
    }

}
