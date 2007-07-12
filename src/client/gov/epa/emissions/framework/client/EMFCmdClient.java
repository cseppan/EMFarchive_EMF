package gov.epa.emissions.framework.client;

import java.util.Date;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.basic.UserService;


public class EMFCmdClient {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default


    private static UserService userAdmin;

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equalsIgnoreCase("Help")) {
            displayHelp();
            return;
        }

        run(args);

    }

    private static void displayHelp() {
        System.out
                .println("Usage\njava "
                        + EMFClient.class.getName()
                        + " [url]"
                        + "\n\turl - location of EMF Services. Defaults to "
                        + DEFAULT_URL
                        + "\n\tspecify '-DUSER_PREFERENCES=<full path to EMFPrefs.txt>' to override location of User Preferences");
    }

    
    /**
     *
     */
    private static void run(String[] args) throws Exception {
        String msg;
        long sTime;
        long eTime;
        
        try
        {
          String url = DEFAULT_URL;
          if (args.length == 1)
              url = args[0];

          System.out.println("Starting EMF Command Client");
          ServiceLocator serviceLocator = new RemoteServiceLocator(url);

          userAdmin=serviceLocator.userService();
          sTime = new Date().getTime();
          msg = userAdmin.getEmfVersion();
          eTime = new Date().getTime();
          
          
          System.out.println("EMF VERSION INFO: [ " + msg + " ] message was acquired in " + (eTime-sTime) + " milliseconds");
          System.out.println("Exiting EMF Command Client");
          
        }
        catch (Exception exc)
        {
            System.out.println("Exception starting client: "+exc.getMessage());
            exc.printStackTrace();
            throw exc;
        }
      }

}
