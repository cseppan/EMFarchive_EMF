// The FrameworkObservable class
// This is the non-GUI base class for objects

package gov.epa.emissions.framework.client.gui;

// Use Java swing classes
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public abstract class FrameworkObservable
{
    
    // Constructor
    public FrameworkObservable()
    {
    }
    
    // showGUI, to create and display the user GUI
    // This receives the address of the user object and whether this is a new
    // user for which there is no valid information yet
    // This method does not return until the user closes the info window
    // This returns a flag, true if the user has successfully updated the information
    // or false if he has canceled the window
    public static boolean showGUI(User user, boolean NewUser)
    {
        // Make the user info window
        UserInformationWindow uiw = new UserInformationWindow();
        // Pass the address of user to the user info window
        uiw.setModel(user);
        // Set up the components of the User GUI
        uiw.setupComponents(NewUser);
        // Load data from the user into the components of the uiw
        uiw.loadDataFromModel(NewUser);
       // Display the window
        uiw.show();
       // Wait for the user info window to close
       // For a JDialog window, the code pauses automatically until user closes window
       // For a JFrame window, this explicit test is needed
       // while (!uiw.getDoneFlag()) { }
       // Get the return flag from the window to pass to the caller
       boolean rf = uiw.getGoodReturn();
       return rf;
    }
    
}
