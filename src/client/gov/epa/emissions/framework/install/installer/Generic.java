package gov.epa.emissions.framework.install.installer;

public class Generic {
	public static final String VERSION = "1/20/2006";
	public static final String SEPARATOR = System.getProperty("line.separator");
	
	public static final String TIME_FORMAT = "MM/dd/yyyy hh:mmaaa";
    public static final String EMF_URL = "http://www.epa.gov/ttn/fera/data/trim/install";
	
	public static final String USER_PARAMETER = "EMFUserInstallPrefs.ini";
    public static final String EMF_PARAMETER = "EMFPrefs.txt";
	public static final String FILE_LIST = "files.txt";
	public static final String UPDATE_FILE = "update.dat";
    public static final String EMF_BATCH_FILE = "EMFClient.bat";
    
    public static final String REMOTE_INPUT_DRIVE = "C:\\";
    public static final String REMOTE_OUTPUT_DRIVE = "C:\\";
    public static final String JAVA_HOME = System.getProperty("java.home");
    public static final String INSTALL_HOME = "C:\\Program Files";
	
	public static final String[] BAT_FILES = {"Aggregator", "Averager", "copyDB", "rungfg", "runmims", "Transposer"};
	public static final String[] TO_DELETE = {"rundave.txt", "runfate.txt", "runtable.txt", "runtrim.txt", "trimvars.txt"};
	
    public static final String EMF_MESSAGE = "<html> <br><br><br><br>" +
            "Installing the Emissions Modeling Framework...";  
    
    public static final String EMF_CLOSE_MESSAGE = "<html><br><br><br><br>" +
            "Installation complete.";
    
}
