// Main.java, the main program for the MainGUI interface
// Joe Capowski   June 3, 2005


package gov.epa.emissions.framework.client.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import gov.epa.emissions.framework.commons.User;



public class Main
{
    
    // The ref variable for the Current user
     public static User CurrentUser = null;
    // Whether he is logged on as administrator
    public static boolean LoggedOnAdmin = false;
 
 
  public static void main(String[] args)
  {
      // Instantiate the User Manager Client
      UserManagerClient umc = new UserManagerClient();
      // Temporarily create 3 Initial Users
      umc.Create3InitialUsers();
      // Create the object for the current user with blank fields
      User CurrentUser = new User("", "", "", "","", "", false, false);
      
      // Because we may oscillate between the login process and the logout process
      // from the MainGui window, create an infinite while loop
      while (true)
      {
         // Create and display the login window
         LoginWindow logwin = new LoginWindow(CurrentUser,umc);
         // The code pauses here until the user finishes with the login window
         // and its handler disposes of it.
         // If there is a good return from the logon of a existing user or the
         // creation of a new user, get the user object from the login window.
         // We must also save externally, outside of the user object,
         // whether he is logged on as administrator
         if (logwin.getGoodReturn())
         {
            CurrentUser = logwin.getCurrentUser();
            LoggedOnAdmin = logwin.getAdmLogin(); 
         }
         
         // Build the main GUI window
         MainGUI mgwin = new MainGUI(CurrentUser, umc, LoggedOnAdmin);
         // Since it is a Frame and not modal, wait for it to be done, then dispose of it
         while (!mgwin.getDoneFlag()) { }
         mgwin.dispose();
      }
  }
}
                
