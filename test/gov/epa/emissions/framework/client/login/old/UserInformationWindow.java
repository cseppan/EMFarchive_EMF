package gov.epa.emissions.framework.client.login.old;

import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.commons.User;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class UserInformationWindow extends JDialog implements ActionListener, FrameworkWindow {
    User user = null;

    boolean GoodReturn = false;

    JLabel fullnamelabel = null;

    JTextField fullnametext = null;

    JLabel affiliationlabel = null;

    JTextField affiliationtext = null;

    JLabel workphonelabel = null;

    JTextField workphonetext = null;

    JLabel emailaddresslabel = null;

    JTextField emailaddresstext = null;

    JLabel usernamelabel = null;

    JTextField usernametext = null;

    JLabel passwordlabel = null;

    JTextField passwordtext = null;

    JLabel confirmpasswordlabel = null;

    JTextField confirmpasswordtext = null;

    JLabel usercanbeadminlabel = null;

    JButton ok = null;

    JButton cancel = null;

    public void setModel(User userin) {
        user = userin;
    }

    // setupComponents, to place components onto the GUI
    public void setupComponents(boolean NewUser) {
        setTitle("User Information");
        if (!NewUser && user.isInAdminGroup())
            setSize(450, 450);
        else
            setSize(450, 400);
        setLocation(300, 200);
        setModal(true);

        GoodReturn = false;

        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.LEFT, 50, 25));

        fullnamelabel = new JLabel("Full Name               ");
        container.add(fullnamelabel);

        // The full name text field
        fullnametext = new JTextField(20);
        container.add(fullnametext);
        fullnametext.addActionListener(this);

        // The affiliation label
        affiliationlabel = new JLabel("Affiliation               ");
        container.add(affiliationlabel);

        // The affiliation text field
        affiliationtext = new JTextField(15);
        container.add(affiliationtext);
        affiliationtext.addActionListener(this);

        // The work phone label
        workphonelabel = new JLabel("Work Phone          ");
        container.add(workphonelabel);

        // The work phone text field
        workphonetext = new JTextField(12);
        container.add(workphonetext);
        workphonetext.addActionListener(this);

        // The email address label
        emailaddresslabel = new JLabel("E-mail Address      ");
        container.add(emailaddresslabel);

        // The email address text field
        emailaddresstext = new JTextField(20);
        container.add(emailaddresstext);
        emailaddresstext.addActionListener(this);

        // The user name label
        usernamelabel = new JLabel("User Name              ");
        container.add(usernamelabel);

        // The user name text field
        usernametext = new JTextField(9);
        container.add(usernametext);
        usernametext.addActionListener(this);
        // For an existing user, don't allow the user to change the user name
        if (!NewUser)
            usernametext.setEnabled(false);

        // The password label
        passwordlabel = new JLabel("Password                ");
        container.add(passwordlabel);

        // The password text field
        passwordtext = new JPasswordField(12);
        container.add(passwordtext);
        passwordtext.addActionListener(this);

        // The confirm password label
        confirmpasswordlabel = new JLabel("Confirm Password");
        container.add(confirmpasswordlabel);

        // The confirm password text field
        confirmpasswordtext = new JPasswordField(12);
        container.add(confirmpasswordtext);
        confirmpasswordtext.addActionListener(this);

        // The User Can Be Administrator label...this cannot be edited
        // Present this only for an existing user who can be an administrator
        if (!NewUser && user.isInAdminGroup()) {
            usercanbeadminlabel = new JLabel("This user can log in as an administrator                     ");
            container.add(usercanbeadminlabel);
        }

        // The OK Button
        ok = new JButton("    OK    ");
        ok.addActionListener(this);
        container.add(ok);

        // The Cancel Button
        cancel = new JButton("  Cancel  ");
        cancel.addActionListener(this);
        container.add(cancel);

    } // End of setupComponents method

    // loadDataFromModel, to get the data from the user object and put it into
    // the GUI components. Also receive whether this is a new user
    public void loadDataFromModel(boolean NewUser) {
        // For an existing user, fill the components with the user's
        // information,
        // except don't display password information.
        if (!NewUser) {
            fullnametext.setText(user.getFullName());
            affiliationtext.setText(user.getAffiliation());
            workphonetext.setText(user.getWorkPhone());
            emailaddresstext.setText(user.getEmailAddr());
            usernametext.setText(user.getUserName());
        }
    }

    // saveDataToModel, to save the data from the components to the user object
    public void saveDataToModel() {
        user.setFullName(fullnametext.getText());
        
        user.setWorkPhone(workphonetext.getText());
        user.setEmailAddr(emailaddresstext.getText());
        try {
            user.setAffiliation(affiliationtext.getText());
            user.setUserName(usernametext.getText());
            user.setPassword(passwordtext.getText());
        } catch (UserException e) {
            throw new RuntimeException("cannot save data to model", e);
        }        
    }

    // getGoodReturn, to get whether this window closed with success
    public boolean getGoodReturn() {
        return GoodReturn;
    }

    public String validate(String FullNameIn, String AffiliationIn, String WorkPhoneIn, String EMailAddrIn,
            String UserNameIn, String PasswordIn, String ConfPasswordIn) {
        // If the user name is too short, return with a message
        if (UserNameIn.length() < 3)
            return "User Name must have at least 3 characters";
        // If the password is too short, return with a message
        if (PasswordIn.length() < 8)
            return "Password must have at least 8 characters";
        // If the confirmed password is too short, return with a message
        if (ConfPasswordIn.length() < 8)
            return "Confirmed password must have at least 8 characters";

        // Check that at least one character of the password is a non-letter
        int Nletters = 0;
        for (int ichar = 0; ichar < PasswordIn.length(); ichar++) {
            if ((PasswordIn.charAt(ichar) >= 'a' && PasswordIn.charAt(ichar) <= 'z')
                    || (PasswordIn.charAt(ichar) >= 'A' && PasswordIn.charAt(ichar) <= 'Z'))
                Nletters++;
        }
        if (Nletters >= PasswordIn.length())
            return "One or more characters of password must be a non-letter";
        // If the user name is the same as the password, return with a message
        if (UserNameIn.equals(PasswordIn))
            return "User Name must be different from Password";
        // If the two passwords don't match, return with a message
        if (!PasswordIn.equals(ConfPasswordIn))
            return "Password does not match Confirm Password";
        // If four other fields are too short, return with a message
        if (FullNameIn.length() < 3)
            return " Full Name must have 2 or more characters";
        if (AffiliationIn.length() < 3)
            return " Affiliation must have 2 or more characters";
        if (WorkPhoneIn.length() < 3)
            return " Work Phone must have 2 or more characters";
        if (EMailAddrIn.length() < 3)
            return " EMail Addrss must have 2 or more characters";
        // Yipee! it passed all the tests, return with its message
        return "OK";
    }

    // Come here when the user interacts with any of the components
    public void actionPerformed(ActionEvent e) {
        // If the user clicks the cancel button, tell the caller that this is
        // a bad return and completely eliminate the window
        if (e.getSource() == cancel) {
            GoodReturn = false;
            dispose();
        }

        if (e.getSource() == ok || e.getSource() == confirmpasswordtext) {
            // Go do a preliminary check of his user-typed info, returns a
            // descriptive
            // string OK or the type of error
            String str = validate(fullnametext.getText(), affiliationtext.getText(), workphonetext.getText(),
                    emailaddresstext.getText(), usernametext.getText(), passwordtext.getText(), confirmpasswordtext
                            .getText());
            // If the name and password are of proper form, put his info into
            // the
            // user object, then eliminate the window indicating success
            if (str.equals("OK")) {
                saveDataToModel();
                GoodReturn = true;
                dispose();
            }
            // Otherwise, there is something wrong with his data. Give him the
            // message
            // and leave the User Information Window on the screen
            else
                JOptionPane.showMessageDialog(this, "Result from Preliminary Info check:  " + str);
        }
    }

}
