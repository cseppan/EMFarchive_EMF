package gov.epa.emissions.framework.client;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public class EmfFrame extends JFrame {

    public EmfFrame() {
        super.setUndecorated(true);
        super.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        
        super.setResizable(false);        
    }

}
