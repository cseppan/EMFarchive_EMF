package gov.epa.emissions.framework.client;

import java.awt.Color;

import javax.swing.JLabel;

public class SingleLineMessagePanel extends MessagePanel {

    private JLabel label;

    public SingleLineMessagePanel() {
        label = new JLabel();
        super.add(label);

        super.setVisible(false);
    }

    public void clear() {
        label.setText("");
    }

    public void setMessage(String message, Color color) {
        clear();

        label.setForeground(color);
        label.setText(message);

        super.setVisible(true);
    }

}
