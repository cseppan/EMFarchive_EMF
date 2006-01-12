package gov.epa.emissions.framework.client.preference;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class UserPreferences extends Properties {

    public static final String EMF_INPUT_DRIVE = "EMFInputDriveLetter";
    
    public static final String EMF_OUTPUT_DRIVE = "EMFOutputDriveLetter";
    
    public static final String EMF_INPUT_PATH = "EMFInputServerDirectory";
    
    public static final String EMF_OUTPUT_PATH = "EMFOutputServerDirectory";

    public static final String EMF_DEFAULT_INPUT_DIR = "EMFDefaultInputDirectory";

    public static final String EMF_DEFAULT_OUTPUT_DIR = "EMFDefaultOutputDirectory";

    public static final String EMF_PREFERENCE = "EMF_PREFERENCE";

    private static UserPreferences userPreferences = null;
    
    public UserPreferences() {
        loadProperties();
    }

    public static UserPreferences getInstance() {
        if (userPreferences == null) {
            userPreferences = new UserPreferences();
        }
        return userPreferences;
    }

    private void loadProperties() {
            File file = new File(getPropertyFile(EMF_PREFERENCE));
            try{
            FileInputStream inStream = new FileInputStream(file);
            load(inStream);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
    }

    private String getPropertyFile(String property) {
        String fileName = System.getProperty(property);
        if (!checkFile(fileName)) {
            System.err.println("The file specifice by '" + property + "' does not exist.");
        }
        return fileName.trim();
    }

    public boolean checkFile(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

}
