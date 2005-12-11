package gov.epa.emissions.framework.client;

import java.awt.Color;

import javax.swing.JLabel;

public class SingleLineMessagePanel extends MessagePanel {

    private JLabel label;

    private String message;

    public SingleLineMessagePanel() {
        label = new JLabel(" ");
        super.add(label);

        super.setVisible(true);
    }

    public void clear() {
        message = "";
        label.setText(" ");
    }

    public void setMessage(String message, Color color) {       
        clear();

        this.message = message;
        label.setForeground(color);
        label.setText(message);

        super.setVisible(true);
        super.validate();
    }

    public String getMessage() {
        return message;
    }

}
