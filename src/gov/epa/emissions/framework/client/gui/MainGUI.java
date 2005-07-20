// The MainGUI class
// Joe Capowski   June 13, 2005

package gov.epa.emissions.framework.client.gui;

// Use Java swing classes
import gov.epa.emissions.framework.commons.User;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;


public class MainGUI extends JFrame implements ActionListener
{
    
    // The Done Flag, which will be set when the user selects Logout, which
    // really means to return to the main program to restart the login process
    boolean DoneFlag;
    
    // The ref variable for the current user object
    User user = null;
    // The ref variable for the User Manager Client
    UserManagerClient umc = null;
    
    // Whether the current user is logged on as an administrator
    boolean LoggedOnAdmin = false;
    
    
    // Constructor...Create the MainGUI window, its menu and components
    public MainGUI(User userin, UserManagerClient umcin, boolean LoggedOnAdminIn)
    {
        // Give the title, size and location for the window
        setTitle("Emissions Modeling Framework Main GUI");
        setSize(600,350);
        setLocation(300,200);
        
        // Store the incoming variables
        user = userin;
        umc = umcin;
        LoggedOnAdmin = LoggedOnAdminIn;
        
        // Clear the Done Flag
        DoneFlag = false;
        
        // Create the menu system for this window
        JMenuBar jmb = new JMenuBar();
        setJMenuBar(jmb);
        JMenu FileMenu = new JMenu("File");
        JMenu ManageMenu = new JMenu("Manage");
        JMenu ToolsMenu = new JMenu("Tools");
        JMenu WindowMenu = new JMenu("Window");
        JMenu HelpMenu = new JMenu("Help");
        jmb.add(FileMenu);
        jmb.add(ManageMenu);
        jmb.add(ToolsMenu);
        jmb.add(WindowMenu);
        jmb.add(HelpMenu);
        
        // Create the items of the File menu and add listeners to them
        JMenuItem ImportDatasetItem = new JMenuItem("Import Dataset");
        JMenuItem LogoutItem = new JMenuItem("Logout");
        JMenuItem ExitItem = new JMenuItem("Exit");
        FileMenu.add(ImportDatasetItem);
        FileMenu.add(LogoutItem);
        FileMenu.add(ExitItem);
        ImportDatasetItem.addActionListener(this);
        LogoutItem.addActionListener(this);
        ExitItem.addActionListener(this);
        
        // Create the items of the Manage menu and add listeners to them
        // Only do the "Manage Users" item if the user is logged on as an admin
        JMenuItem DatasetSectorsItem = new JMenuItem("Dataset Sectors");
        JMenuItem DatasetTypesItem = new JMenuItem("Dataset Types");
        ManageMenu.add(DatasetSectorsItem);
        ManageMenu.add(DatasetTypesItem);
        DatasetSectorsItem.addActionListener(this);
        DatasetTypesItem.addActionListener(this);
        if (LoggedOnAdmin)
        {
            JMenuItem UsersItem = new JMenuItem("Users");
            ManageMenu.add(UsersItem);
            UsersItem.addActionListener(this);
        }
                
        // Create the items of the Tools menu and add listeners to them
        JMenuItem JoeSummaryItem = new JMenuItem("Joes Summary");
        JMenuItem EditUserInfoItem = new JMenuItem("Edit User Info");
        JMenuItem OptionsItem = new JMenuItem("Options");
        ToolsMenu.add(JoeSummaryItem);
        ToolsMenu.add(EditUserInfoItem);
        ToolsMenu.add(OptionsItem);
        JoeSummaryItem.addActionListener(this);
        EditUserInfoItem.addActionListener(this);
        OptionsItem.addActionListener(this);
        
        // Create the items of the Window menu and add listeners to them
        JMenuItem ShowStatusItem = new JMenuItem("Show Status");
        JMenuItem RefreshAllItem = new JMenuItem("Refresh All");
        WindowMenu.add(ShowStatusItem);
        WindowMenu.add(RefreshAllItem);
        ShowStatusItem.addActionListener(this);
        RefreshAllItem.addActionListener(this);
        
        // Create the items of the Help menu and add listeners to them
        JMenuItem UserGuideItem = new JMenuItem("User Guide");
        JMenuItem DocumentationItem = new JMenuItem("Documentation");
        JMenuItem AboutItem = new JMenuItem("About");
        HelpMenu.add(UserGuideItem);
        HelpMenu.add(DocumentationItem);
        HelpMenu.add(AboutItem);
        UserGuideItem.addActionListener(this);
        DocumentationItem.addActionListener(this);
        AboutItem.addActionListener(this);
        
        // Display the MainGUI window
        show();
    }
    
    
    // getDoneFlag, to return the done flag to the caller
    public boolean getDoneFlag()
    {
        return DoneFlag;
    }
    
    
   // Come here when he selects any of the menu items
    public void actionPerformed(ActionEvent e)
    {
        // Get the text of the command that caused this event
        String ActionText = e.getActionCommand();
        
        // If he selected File-Import Dataset, print a message
        if (ActionText.equals("Import Dataset"))
                JOptionPane.showMessageDialog(this, "Import Dataset not ready");
        
        // If he selected File-Logout, ask for a confirmation, then set the
        // done flag so the main program will restart the login process
        if (ActionText.equals("Logout"))
        {
            int n = JOptionPane.showConfirmDialog
               (this, "Do you really want to log out?", "Please Confirm", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) DoneFlag = true;
        }
        
        // If he selected File-Exit, ask for a confirmation, then totally quit
        if (ActionText.equals("Exit"))
        {
            int n = JOptionPane.showConfirmDialog
               (this, "Do you really want to quit?", "Please Confirm", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) System.exit(0);
        }
        
          // If he selected Manage-Dataset Sectors, print a message
        if (ActionText.equals("Dataset Sectors"))
                JOptionPane.showMessageDialog(this, "Dataset Sectors not ready");
        
        // If he selected Manage-Dataset Types, print a message
        if (ActionText.equals("Dataset Types"))
                JOptionPane.showMessageDialog(this, "Dataset Types not ready");
        
        // If he selected Manage-Uses, print a message
        // This can only occur if he is logged on as an administrator
        if (ActionText.equals("Users"))
                JOptionPane.showMessageDialog(this, "Users not ready");
        
        // If he selected Tools-Joe's Summary, display a summary of all the users
        if (ActionText.equals("Joes Summary"))
        {
            // Create the all users info window
            AllUsersInformationWindow auiw = 
                new AllUsersInformationWindow(user, umc);
            // The code pauses here until the auiw window is closed
        }
       
        // If he selected Tools-Edit User Info, build the user information window
        if (ActionText.equals("Edit User Info"))
        {
            // Create the user info window, pass it the addr of the user, and
            // indicate that this is not a new user
            boolean Success = FrameworkObservable.showGUI(user,false);
            // The code pauses here until the user has updated the information
            // and closed the window
            // If the return was successful, update the user info in the data base
            if (Success)
            {
                String result = umc.updateUser(user);
                // If the user could not be updated, print why
                if (!result.equals("Success"))
                   JOptionPane.showMessageDialog(this, result);
            }
        }
        
        // If he selected Tools-Options, print a message
        if (ActionText.equals("Options"))
                JOptionPane.showMessageDialog(this, "Options not ready");
        
        // If he selected Window-Show Status, print a message
        if (ActionText.equals("Show Status"))
                JOptionPane.showMessageDialog(this, "Show Status not ready");
        
        // If he selected Window-Refresh All, print a message
        if (ActionText.equals("Refresh All"))
                JOptionPane.showMessageDialog(this, "Refresh All not ready");
        
        // If he selected Help-User Guide, print a message
        if (ActionText.equals("User Guide"))
                JOptionPane.showMessageDialog(this, "User Guide not ready");
        
        // If he selected Help-Documentation, print a message
        if (ActionText.equals("Documentation"))
                JOptionPane.showMessageDialog(this, "Documentation not ready");
        
        // If he selected Help-About, print a message
        if (ActionText.equals("About"))
                JOptionPane.showMessageDialog(this, "About not ready");
        
    }
}
        