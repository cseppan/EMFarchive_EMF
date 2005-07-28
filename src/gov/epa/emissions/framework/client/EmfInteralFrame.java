package gov.epa.emissions.framework.client;

import javax.swing.JInternalFrame;

public class EmfInteralFrame extends JInternalFrame {

    public EmfInteralFrame(String title) {
        super(title, 
                true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable
    }

}
