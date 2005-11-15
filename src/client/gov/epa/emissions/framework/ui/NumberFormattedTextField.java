package gov.epa.emissions.framework.ui;

import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class NumberFormattedTextField extends JFormattedTextField {

    public NumberFormattedTextField(int min, int max, int size, Action action) {
        super.setFormatterFactory(new DefaultFormatterFactory(formatter(max)));
        super.setValue(new Integer(min));
        super.setColumns(size);

        addActionForEnterKeyPress(action);
    }

    private void addActionForEnterKeyPress(Action action) {
        super.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        super.getActionMap().put("check", action);
    }

    private NumberFormatter formatter(int max) {
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
        formatter.setMinimum(new Integer(0));
        formatter.setMaximum(new Integer(max));

        return formatter;
    }

}
