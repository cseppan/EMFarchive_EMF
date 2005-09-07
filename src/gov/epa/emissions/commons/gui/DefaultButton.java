package gov.epa.emissions.commons.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.KeyStroke;

public class DefaultButton extends JButton {

    public DefaultButton(String name, final Action action) {
        super(name);
        super.setName(name);

        addActionForEnterKeyPress(name, action);
        super.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                action.actionPerformed(event);
            }
        });
    }

    private void addActionForEnterKeyPress(String name, Action action) {
        super.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), name);
        super.getActionMap().put(name, action);
    }

}
