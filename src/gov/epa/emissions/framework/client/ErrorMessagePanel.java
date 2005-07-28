package gov.epa.emissions.framework.client;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ErrorMessagePanel extends JPanel {

    private JLabel errorMessage;

    public ErrorMessagePanel() {
        errorMessage = new JLabel();
        super.add(errorMessage);
    }

    public void setMessage(String message) {
        clear();
        errorMessage.setForeground(Color.RED);
        errorMessage.setText(message);
    }

    public void clear() {
        errorMessage.setText("");
    }

}
