package gov.epa.emissions.framework.client.preference;

import gov.epa.emissions.framework.EmfException;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserPreferences extends Properties {

    private static Log log = LogFactory.getLog(UserPreferences.class);

    public static final String EMF_INPUT_DRIVE = "EMFInputDriveLetter";

    public static final String EMF_OUTPUT_DRIVE = "EMFOutputDriveLetter";

    public static final String EMF_INPUT_PATH = "EMFInputServerDirectory";

    public static final String EMF_OUTPUT_PATH = "EMFOutputServerDirectory";

    public static final String EMF_DEFAULT_INPUT_DIR = "EMFDefaultInputDirectory";

    public static final String EMF_DEFAULT_OUTPUT_DIR = "EMFDefaultOutputDirectory";

    public static final String EMF_PREFERENCE = "EMF_PREFERENCE";

    public UserPreferences() throws EmfException {
        loadProperties();
    }

    private void loadProperties() throws EmfException {
        try {
            FileInputStream inStream = new FileInputStream(getFile());
            load(inStream);
        } catch (Exception e) {
            log.error("Cannot load user preferences file.");
            throw new EmfException("Cannot load user preferences file.");
        }
    }

    private File getFile() {
        String property = System.getProperty(EMF_PREFERENCE);
        if (property != null && new File(property).exists())
            return new File(property);

        return new File(System.getProperty("user.home"), "EMFPrefs.txt");
    }

    public boolean checkFile(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public String getInputDir() {
        return getProperty(EMF_INPUT_DRIVE) + ":\\" + getProperty(EMF_DEFAULT_INPUT_DIR);
    }

    public String getOutputDir() {
        return getProperty(EMF_OUTPUT_DRIVE) + ":\\" + getProperty(EMF_DEFAULT_OUTPUT_DIR);
    }

    public String getServerInputDir() {
        return getProperty(EMF_INPUT_PATH) + "/" + getProperty(EMF_DEFAULT_INPUT_DIR);
    }

    public String getServerOutputDir() {
        return getProperty(EMF_OUTPUT_PATH) + "/" + getProperty(EMF_DEFAULT_OUTPUT_DIR);
    }

}
