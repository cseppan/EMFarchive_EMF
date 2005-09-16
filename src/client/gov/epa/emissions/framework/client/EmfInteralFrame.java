package gov.epa.emissions.framework.client;

import javax.swing.JInternalFrame;

public abstract class EmfInteralFrame extends JInternalFrame implements EmfView, ManagedView {

    public EmfInteralFrame(String title) {
        super(title, true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable
    }

    public void close() {
        super.dispose();
    }

    public void bringToFront() {
        super.toFront();
    }

    public void display() {
        super.setVisible(true);
    }
}
