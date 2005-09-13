package gov.epa.emissions.commons.gui;

import javax.swing.JPasswordField;

public class PasswordField extends JPasswordField {

    public PasswordField(String name, int size) {
        super(size);
        super.setName(name);
    }

}
