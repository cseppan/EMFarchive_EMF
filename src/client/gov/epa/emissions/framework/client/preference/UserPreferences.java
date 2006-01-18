package gov.epa.emissions.framework.client.preference;

import gov.epa.emissions.framework.EmfException;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserPreferences {

    private static Log log = LogFactory.getLog(UserPreferences.class);

    public static final String EMF_PREFERENCE = "EMF_PREFERENCE";

    private Properties props;

    public UserPreferences() throws EmfException {
        props = new Properties();
        try {
            FileInputStream inStream = new FileInputStream(getFile());
            props.load(inStream);
        } catch (Exception e) {
            log.error("Cannot load user preferences file " + getFile().getAbsolutePath());
            throw new EmfException("Cannot load user preferences file");
        }
    }

    public UserPreferences(Properties props) {
        this.props = props;
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

    private String property(String name) {
        return props.getProperty(name);
    }

    public String inputFolder() {
        return property("local.input.drive") + property("default.input.folder");
    }

    public String outputFolder() {
        return property("local.output.drive") + property("default.output.folder");
    }

    public String mapLocalInputPathToRemote(String localPath) {
        String local = inputFolder();
        String remote = property("remote.input.drive") + property("default.input.folder");

        String path = remote + localPath.substring(local.length());
        return path.replace('\\', '/');
    }

    public String mapLocalOutputPathToRemote(String localPath) {
        String local = outputFolder();
        String remote = property("remote.output.drive") + property("default.output.folder");

        String path = remote + localPath.substring(local.length());
        return path.replace('\\', '/');
    }
}
