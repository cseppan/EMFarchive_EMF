package gov.epa.emissions.framework.client;

import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

public abstract class EmfInternalFrame extends JInternalFrame implements EmfView, ManagedView {

    public EmfInternalFrame(String title) {
        super(title, true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable
    }

    public void bringToFront() {

        super.toFront();
        try {
            super.setIcon(false);
            super.setSelected(true);
        } catch (PropertyVetoException e) {
            throw new RuntimeException("could not bring the window - " + super.getTitle() + " to front of the desktop");
        }
        display();
    }

    public void display() {
        super.getDesktopPane().setSelectedFrame(this);
        super.setVisible(true);
    }

}
