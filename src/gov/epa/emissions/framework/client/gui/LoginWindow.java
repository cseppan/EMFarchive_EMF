 // This class defines the Login Window
// Joe Capowski   June 3, 2005

// The strategy here is that when we exit this window with a good return, the
// current user object will have been updated with the latest user information.

package gov.epa.emissions.framework.client.gui;

// Use Java swing classes
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import gov.epa.emissions.framework.client.transport.EMFUserAdmin;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.User;



public class LoginWindow extends JDialog implements ActionListener
{
    
    // The ref variable of the current user
    User CurrentUser = null;

    
    // Current user name and password
    String CurrentUserName = null;
    String CurrentPassword = null;
    // Whether he tries to log in as the administrator
    boolean AdmLogin = false;
    
    // The ref variable of the user manager client
    UserManagerClient umc = null;
    
    
    // The names of components that are placed in the window
    JLabel usernamelabel = null;
    JTextField usernametext = null;
    JLabel passwordlabel = null;
    JPasswordField passwordtext = null;
    JButton ok = null;
    JButton cancel = null;
    JButton newuser = null;
    JButton resetpassword = null;
    JCheckBox loginadmckbox = null;
    
    // The type of return, true = he entered info, false = he canceled
    boolean GoodReturn = false;
     
    
    // Constructor; create the login window and create its components
    public LoginWindow(User CurrentUserIn,UserManagerClient umcin)
    {
        
        // Save the incoming reference variables
        CurrentUser = CurrentUserIn;
        umc = umcin;
        
        // Set the parameters for the window
        setTitle("Emissions Modeling Framework Login");
        setSize(300,350);
        setLocation(400,200);
        setModal(true);
        
        // Get the content pane to which to add buttons, labels, etc.
        Container container = getContentPane();
        // Set flow layout aligned left with horizontal and vertical gaps
        container.setLayout(new FlowLayout(FlowLayout.LEFT, 50,25));
        
     
        // In flow layout order, create the objects for the frame's container,
        // create and add listeners to the relevant ones, and add the objects
        // to the frame's container
        
        // The user name label
        usernamelabel = new JLabel("User Name");
        container.add(usernamelabel);
        
        // The user name text field
        usernametext = new JTextField(9);
        container.add(usernametext);
        usernametext.addActionListener(this);
        
        // The password label
        passwordlabel = new JLabel("Password ");
        container.add(passwordlabel);

        // The password text field
        passwordtext = new JPasswordField(9);
        container.add(passwordtext);
        passwordtext.addActionListener(this);
        
        // The OK Button
        ok = new JButton ("    OK    ");
        ok.addActionListener(this);
        container.add(ok);
        
        // The Cancel Button
        cancel = new JButton ("  Cancel  ");
        cancel.addActionListener(this);
        container.add(cancel);
        
        // The New User Button
        newuser = new JButton("Create New User");
        newuser.addActionListener(this);
        container.add(newuser);
        
        // The Reset Password Button
        resetpassword = new JButton ("Reset Forgotten Password");
        resetpassword.addActionListener(this);
        container.add(resetpassword);
                
        // The Login as Administrator Checkbox
        loginadmckbox = new JCheckBox("Login as Administrator");
        loginadmckbox.addActionListener(this);
        container.add(loginadmckbox);
        
        // Display the frame
        show();
        
    }  // End of constructor
    
    
    // Here are methods used by this class
    // getCurrentUser, to return the current user object ref variable
    public User getCurrentUser()
    {
        return CurrentUser;
    }
    
    // getCurrentUserName, to return the current username
    public String getCurrentUserName()
    {
        return CurrentUserName;
    }
    
    // getCurrentPassword, to return the current password
    public String getCurrentPassword()
    {
        return CurrentPassword;
    }
    
    // getAdmLogin, to return whether the user logged in as administrator
    public boolean getAdmLogin()
    {
        return AdmLogin;
    }
    
    //  getGoodReturn, to return whether the user successfully used the window     
    public boolean getGoodReturn()
    {
        return GoodReturn;
    }
 

    
    // Come here when the user interacts with any of the components
    public void actionPerformed(ActionEvent e)
    {
        // If the user pressed OK or if he hit the enter key in the password box,
        // start to process the user information
        if (e.getSource() == ok  ||  e.getSource() == passwordtext)
        {
            // Get the user info from the text boxes
            CurrentUserName = usernametext.getText();
            CurrentPassword = passwordtext.getText();
            AdmLogin = loginadmckbox.isSelected();
            // Go authenticate the user

            // This statement calls Joe's method
    //      String UserStatus = umc.Authenticate(CurrentUserName,CurrentPassword,AdmLogin);
            // Print what I'm sending to Conrad
            JOptionPane.showMessageDialog(this,"To Conrad authen  " + CurrentUserName +
            		"   " + CurrentPassword  + "    " + AdmLogin);
            // These statements call Conrad's method
            EMFUserAdminTransport uat = new EMFUserAdminTransport();
            String UserStatus =
            	uat.authenticate(CurrentUserName,CurrentPassword,AdmLogin);
        	// Temp print out Conrad's return
            JOptionPane.showMessageDialog(this,"Conrad from authen  " + UserStatus);
                        
            // If the user is authentic, get the user object and delete the window
            // indicating success
            if (UserStatus.equals("Valid"))
            {    
                // This calls Joe's method
          //    CurrentUser = umc.getUser(CurrentUserName);
            	// This calls Conrad's method
            	CurrentUser = uat.getUser(CurrentUserName);
            	// Temp print out Conrad's return
                JOptionPane.showMessageDialog(this,"Conrad from getUser  " + CurrentUser);
                GoodReturn = true;
                dispose();
            }
            // If the user is not authentic, print the results of the authentication
            // and remain in the login window
            else JOptionPane.showMessageDialog(this,"Login Fails:  " + UserStatus);
        }

        // If he clicks Cancel, get a confirmation, then quit the program
        if (e.getSource() == cancel)
        {
            int n = JOptionPane.showConfirmDialog
               (this, "Do you really want to quit?","Please Confirm", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION)
            {
                System.exit(0);
            }
        }
        
        // If he clicks New User, start the user information window
        if (e.getSource() == newuser)
        {
            // Create the user info window, pass it the addr of the user, and
            // indicate that this is a new user
            boolean Success = FrameworkObservable.showGUI(CurrentUser,true);
            // The code pauses here until the user has updated the information
            // and closed the window
            // If the UIW closed with success, deal with the returned data
            // First, get the address of the user information window
            if (Success)
            {
               // Create a new user in the database with this user object's info
               String Result = umc.createUser(CurrentUser);
               // If the user was created successfully, display a message, delete the
               // login window, and return with success to the main program
               if (Result.equals("Success"))
               {
                   JOptionPane.showMessageDialog(this, "New user created in data base successfully");
                   GoodReturn = true;
                   dispose();
               }
               // If the user could not be created, display the returned message, delete the
               // window completely, and return with failure to the main program
               else
               {
                   JOptionPane.showMessageDialog(this, "Result from creating new user  " + Result);
                   GoodReturn = false;
                   dispose();
               }
            }
        }

        // If he clicks Reset Password, call the method that sends him an email
        // with a newly assigned password.  First, ask for a confirmation
        if (e.getSource() == resetpassword)
        {
            int n = JOptionPane.showConfirmDialog
               (this, "Do you really want us to assign you a new password via email?",
                    "Please Confirm", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION)
            {
                // Here he replied yes.  Call the method that does the work.
                String result = umc.resetPassword();
            }
        }
   }
}
               
