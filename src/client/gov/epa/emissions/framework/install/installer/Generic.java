/*
 * Created on Sep 20, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.epa.emissions.framework.install.installer;

/**
 * @author Qun He, CEP, UNC Chapel Hill
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Generic {
	public static final String VERSION = "1/17/2006";
	public static final String SEPARATOR = System.getProperty("line.separator");
	
	public static final String TIME_FORMAT = "MM/dd/yyyy hh:mmaaa";
	
	public static final String USER_PARAMETER = "EMFPrefs.txt";
	public static final String FILE_LIST = "files.txt";
	public static final String UPDATE_FILE = "update.dat";
    
    public static final String REMOTE_INPUT_DRIVE = "C:\\";
    public static final String REMOTE_OUTPUT_DRIVE = "C:\\";
    public static final String JAVA_HOME = System.getProperty("java.home");
    public static final String INSTALL_HOME = "C:\\Program Files";
	
	public static final String[] BAT_FILES = {"Aggregator", "Averager", "copyDB", "rungfg", "runmims", "Transposer"};
	public static final String[] TO_DELETE = {"rundave.txt", "runfate.txt", "runtable.txt", "runtrim.txt", "trimvars.txt"};
	
    public static final String EMF_MESSAGE = "<html> Welcom to user EMF! <br><br><br>" +
            "The current version is a beta version.<br><br>" +  
            "We wish you the best of using this wonderful product.<br><br>" +
            "CEP, UNC at Chapel Hill </html>";
}
