package gov.epa.emissions.framework.install.installer;

public class Constants {
	public static final String VERSION = "1/26/2006";
	public static final String SEPARATOR = System.getProperty("line.separator");
	
	public static final String TIME_FORMAT = "MM/dd/yyyy hh:mmaaa";
    public static final String EMF_URL = "http://www.cep.unc.edu/empd/projects/emf/install/";
	public static final String SERVER_ADDRESS = "http://emf.rtpnc.epa.gov:8080/emf/services";
    
	public static final String INSTALLER_PREFERENCES_FILE = "EMFUserInstallPrefs.ini";
    public static final String EMF_CLIENT_PREFERENCES_FILE = "EMFPrefs.txt";
	public static final String FILE_LIST = "files.txt";
	public static final String UPDATE_FILE = "update.dat";
    public static final String EMF_BATCH_FILE = "EMFClient.bat";
    
    public static final String REMOTE_INPUT_DIR = "T:\\emf_data\\input";
    public static final String REMOTE_OUTPUT_DIR = "T:\\emf_data\\output";
    public static final String JAVA_HOME = System.getProperty("java.home");
    public static final String INSTALL_HOME = "C:\\Program Files\\EMFClient";
	
    // TBD: finish removing these 
	public static final String[] BAT_FILES = {"runmims"};
	public static final String[] TO_DELETE = {"rundave.txt"};
	
    public static final String EMF_MESSAGE = "<html> <br><br><br><br>" +
            "Installing the Emissions Modeling Framework...";  
    
    public static final String EMF_CLOSE_MESSAGE = "<html><br><br><br><br>" +
            "Installation complete.";
    
}
