package gov.epa.emissions.framework.ui;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class IconButton extends JButton {

    public IconButton(String name, String tooltip, ImageIcon icon) {
        super(icon);
        super.setName(name);
        super.setBorderPainted(false);
        super.setToolTipText(tooltip);
        super.setVerticalTextPosition(AbstractButton.BOTTOM);
        super.setHorizontalTextPosition(AbstractButton.CENTER);
    }

}
