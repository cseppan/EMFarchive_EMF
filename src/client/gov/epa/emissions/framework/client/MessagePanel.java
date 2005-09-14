package gov.epa.emissions.framework.client;

import java.awt.Color;

import javax.swing.JPanel;

public abstract class MessagePanel extends JPanel {

    protected MessagePanel() {
        super.setName("messagePanel");
    }

    public void setError(String error) {
        setMessage(error, Color.RED);
    }

    public void setMessage(String message) {
        setMessage(message, Color.BLUE);
    }

    abstract public void clear();

    abstract public void setMessage(String message, Color color);

    abstract public String getMessage();

}