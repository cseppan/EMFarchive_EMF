// FrameworkFrame, the GUI base class

package gov.epa.emissions.framework.client.gui;

// Use Java swing classes
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public abstract class FrameworkFrame extends JDialog implements FrameworkWindow
{
    
    
    // Constructor
    public FrameworkFrame()
    {
    }
    
    
    // Unused methods of the FrameworkWindow Interface
    public void setupComponents() { }
    public void loadDataFromModel() { }
    public void saveDataToModel() { }
    public void setModel(FrameworkObservable fro) { }
    
}
