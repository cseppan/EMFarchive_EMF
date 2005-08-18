package gov.epa.emissions.framework.client;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class MessagePanel extends JPanel {

    private JLabel label;

    public MessagePanel() {
        label = new JLabel();
        super.add(label);
    }

    public void setError(String error) {
        clear();
        label.setForeground(Color.RED);
        label.setText(error);
    }

    public void clear() {
        label.setText("");
    }

    public void setMessage(String message) {
        clear();
        label.setForeground(Color.BLUE);
        label.setText(message);
    }

}
