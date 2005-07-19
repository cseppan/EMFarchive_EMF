//All Users Information Window, to display a summary of all users
//Joe Capowski     July 2005

package gov.epa.emissions.framework.client.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class AllUsersInformationWindow extends JDialog
{
 // The ref variable for the current user object
 User CurrentUser = null;
 // The ref variable for the User Manager Client
 UserManagerClient umc = null;
 
 // The labels that will present info about each user
 JLabel Label = null;

 
 // Constructor
 public AllUsersInformationWindow(User CurrentUserIn, UserManagerClient umcin)
 {
     // Save the incoming reference variables
     CurrentUser = CurrentUserIn;
     umc = umcin;
     // Set various parameters for the window
     setTitle("All Users Information");
     setSize(700,300);
     setLocation(100,250);
     setModal(true);
     
     // Get the content pane to which to add buttons, labels, etc.
     Container container = getContentPane();
     // Set flow layout aligned left with horizontal and vertical gaps
     container.setLayout(new FlowLayout(FlowLayout.LEFT, 50,25));
  
     // In flow layout order, create the objects for the frame's container,
     // and add the objects to the frame's container
     int nusers = umc.getNumUsers();
     // Place all the users onto labels and into the window
     for (int index = 0; index < nusers; index++)
     {
         User ui = umc.getUser(index);
         Label = new JLabel((index+1) +
                            "       " + ui.getFullName() +
                            "       " + ui.getAffiliation() +
                            "       " + ui.getWorkPhone() +
                            "       " + ui.getEmailAddr() +
                            "       " + ui.getUserName() +
                            "       " + ui.getPassword() +
                            "       " + ui.getCanBeAdmin()     );
         container.add(Label);
     }
     // Display the name of the current user
     Label = new JLabel(" The user currently logged in is   " + CurrentUser.getUserName());
     container.add(Label);
     // Display the window
     show();
 }

}