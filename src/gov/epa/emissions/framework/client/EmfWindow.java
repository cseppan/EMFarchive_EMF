package gov.epa.emissions.framework.client;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class EmfWindow extends JFrame {

    public EmfWindow() throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(true);
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");        
    }

}
