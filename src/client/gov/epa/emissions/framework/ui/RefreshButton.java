package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.services.EmfException;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class RefreshButton extends Button {

    public RefreshButton(final RefreshObserver observer, String message, final MessagePanel messagePanel) {
        super("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    observer.doRefresh();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });

        super.setIcon(refreshIcon(message));
        super.setToolTipText(message);
        super.setBorderPainted(false);
    }

    private ImageIcon refreshIcon(String message) {
        return new ImageResources().refresh(message);
    }

}
