// Support.java, to provide some support functions
// Joe Capowski  June 2005


package gov.epa.emissions.framework.client.login;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Support extends JFrame
{

    // Constructor
    public Support() {
    }
    
    // DisplayMessage, to display a message
    public void DisplayMessage(String str)
    {
       JOptionPane.showMessageDialog(this, str);
    }
    
    
}

        
