package gov.epa.emissions.framework.client.login.old;

import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.commons.User;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class UserManagerClient extends JFrame {

    int Nusers = 0;
    int NUSERSMAX = 50;
    User user[] = new User[NUSERSMAX];

    public String Authenticate(String UserNameIn, String PasswordIn, boolean AdmIn) {
        // If there are no users, return that there is no user
        if (Nusers <= 0)
            return "There are no users in the data base";
        // Loop thru the usernames, looking for a match. Save the user number
        int UserNumber = -1;
        for (int i = 0; i < Nusers; i++) {
            if (UserNameIn.equals(user[i].getUserName()))
                UserNumber = i;
        }
        // Here we couldn't find the user
        if (UserNumber == -1)
            return "Incorrect User Name";
        // Check the password for this user
        if (!PasswordIn.equals(user[UserNumber].getPassword()))
            return "Incorrect Password";
        // Here the user exists. If he didn't log in as administrator he is good
        if (!AdmIn)
            return "Valid";
        // If he did, check that he is allowed to
        if (user[UserNumber].isInAdminGroup())
            return "Valid";
        return "Cant Be Admin";
    }

    // createUser, to create a user in the data base
    // This receives the aleady-created user object, and puts its info into
    // the data base.
    // Its return a string with these possible values
    // "Success" the user was successfully created
    // "User Name Exists" failure because the user name already exists
    public String createUser(User userin) {
        // Loop through the user names and see if the incoming one matches any
        // existing user name
        String UserNameIn = userin.getUserName();
        for (int iuser = 0; iuser < Nusers; iuser++) {
            if (UserNameIn.equals(user[iuser].getUserName()))
                return "User Name Exists";
        }
        // Here it will work.....Add the user to the list of user objects and
        // return
        Nusers++;
        user[Nusers - 1] = userin;
        return "Success";
    }

    // updateUser, to update the information in the database for an existing
    // user
    // It returns a string with these values
    // "Success" for a successful update
    // Other strings only for communications problems, to be defined
    public String updateUser(User userin) {
        // Loop thru the user objects, looking for a match of user names.
        // Save the user number
        int UserNumber = -1;
        for (int i = 0; i < Nusers; i++) {
            String usernamein = userin.getUserName();
            String usernametest = user[i].getUserName();
            if (usernamein.equals(usernametest))
                UserNumber = i;
        }
        // Save the data for this user
        user[UserNumber].setFullName(userin.getFullName());
        user[UserNumber].setAffiliation(userin.getAffiliation());
        user[UserNumber].setWorkPhone(userin.getWorkPhone());
        user[UserNumber].setEmailAddr(userin.getEmailAddr());
        try {
            user[UserNumber].setUserName(userin.getUserName());
        } catch (UserException e) {
           throw new RuntimeException("update user failure", e);
        }
        user[UserNumber].setPassword(userin.getPassword());
        user[UserNumber].setInAdminGroup(userin.isInAdminGroup());
        user[UserNumber].setAcctDisabled(userin.isAcctDisabled());
        return "Success";
    }

    // resetPassword, to send the user an email with a new password
    // The returns a string whose values are "Success" or the three
    // communications-relatd error messages
    public String resetPassword() {
        // Temporarily, just print a message
        JOptionPane.showMessageDialog(this, "Reset Password is not ready");
        return "Success";
    }

    // getUser, to return the ref variable of the current user. This assumes
    // that
    // the user is valid and is in the current list.
    public User getUser(String UserNameIn) {
        for (int i = 0; i < Nusers; i++) {
            if (UserNameIn.equals(user[i].getUserName()))
                return user[i];
        }
        // To satisfy the compiler, should there be a failure, return the first
        // user
        return user[0];
    }

    // Create3InitialUsers, a temporary method to do this
    public void Create3InitialUsers() {
        // Temporarily create several new users with names and passwords
        Nusers = 3;
        user[0] = new User("Robert", "UNC", "966-1234", "bob@unc.edu", "Bob", "bob12345", true, false);
        user[1] = new User("Joseph", "UNC", "966-2345", "joe@unc.edu", "Joe", "joe12345", true, false);
        user[2] = new User("Suzanne", "UNC", "966-3456", "suzy@unc.edu", "Suzy", "suzy12345", false, false);
    }

    // getNumUsers, to return the current number of users
    public int getNumUsers() {
        return Nusers;
    }

    // getUser(i), to return the ref variable of user number i, i from 0 thru
    // Nusers-1
    public User getUser(int index) {
        return user[index];
    }

}
