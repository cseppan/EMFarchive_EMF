package gov.epa.emissions.framework.client;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MultiLineMessagePanel extends MessagePanel {

    private JTextArea textArea;

    private String message;

    public MultiLineMessagePanel(Dimension size) {
        textArea = new JTextArea();
        textArea.setRows(2);
        textArea.setSize(size);
        textArea.setLineWrap(true);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        super.add(scrollPane);

        super.setVisible(false);
    }

    public void setMessage(String message, Color color) {
        clear();
        
        this.message = message;
        textArea.setForeground(color);
        textArea.setText(message);
        textArea.setCaretPosition(0);
        textArea.setToolTipText(message);

        super.setVisible(true);
    }

    public void clear() {
        message = "";
        textArea.setText("");
    }

    public String getMessage() {
        return message;
    }

}
