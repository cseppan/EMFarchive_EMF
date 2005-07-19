// User.java, a class that describes each user
// This is temporary, it will be replaced by the transport class that Conrad
// is now writing, because the real database of users will be stored on a server.
// Joe Capowski   June 15, 2005

package gov.epa.emissions.framework.client.gui;

// Use Java swing classes
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.lang.String;

public class User extends FrameworkObservable
{
    
    // The state variables that define the user
    private String fullName = null;
    private String affiliation = null;
    private String workPhone = null;
    private String emailAddr = null;
    private String userName = null;
    private String password =  null;
    private boolean canBeAdmin = false;
    private boolean acctDisabled = false;
    
    // Constructor to create a new instance of user from incoming information
    public User(String FNin, String AFin, String WPin, String EMin,
                String UNin, String PWin, boolean CAin, boolean ADin)
    {
        // Set the state variables from the incoming information
        fullName = FNin;
        affiliation = AFin;
        workPhone = WPin;
        emailAddr = EMin;
        userName = UNin;
        password = PWin;
        canBeAdmin = CAin;
        acctDisabled = ADin;
     }
    
    // The getters for each state variables
    public String getFullName()     { return fullName;     }
    public String getAffiliation()  { return affiliation;  }
    public String getWorkPhone()    { return workPhone;    }
    public String getEmailAddr()    { return emailAddr;    }
    public String getUserName()     { return userName;     }
    public String getPassword()     { return password;     }
    public boolean getCanBeAdmin()  { return canBeAdmin;   }
    public boolean getAcctDisabled(){ return acctDisabled; }
    
    // The setters for each state variable
    public void setFullName(String FNin)   { fullName = FNin; }
    public void setAffiliation(String AFin)   { affiliation  = AFin; }
    public void setWorkPhone(String WPin)   { workPhone = WPin; }
    public void setEmailAddr(String EMin)   { emailAddr = EMin; }
    public void setUserName(String UNin)   { userName  = UNin; }
    public void setPassword(String PWin)   { password = PWin; }
    public void setCanBeAdmin(boolean CAin)   { canBeAdmin = CAin; }
    public void setAcctDisabled(boolean ADin)   { acctDisabled = ADin; }
    
}

        
