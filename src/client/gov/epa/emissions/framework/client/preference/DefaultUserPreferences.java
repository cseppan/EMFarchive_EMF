package gov.epa.emissions.framework.client.preference;

import gov.epa.emissions.framework.services.EmfException;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultUserPreferences implements UserPreference {

    private static final String DEFAULT_INPUT_FOLDER = "default.input.folder";

    private static final String REMOTE_OUTPUT_DRIVE = "remote.output.drive";

    private static final String DEFAULT_OUTPUT_FOLDER = "default.output.folder";

    private static final String LOCAL_OUTPUT_DRIVE = "local.output.drive";

    private static final String REMOTE_INPUT_DRIVE = "remote.input.drive";

    private static final String LOCAL_INPUT_DRIVE = "local.input.drive";

    private static Log log = LogFactory.getLog(DefaultUserPreferences.class);

    public static final String EMF_PREFERENCE = "USER_PREFERENCES";

    private Properties props;

    public DefaultUserPreferences() throws EmfException {
        props = new Properties();
        try {
            FileInputStream inStream = new FileInputStream(getFile());
            props.load(inStream);
        } catch (Exception e) {
            log.error("Cannot load user preferences file " + getFile().getAbsolutePath(), e);
            throw new EmfException("Cannot load user preferences file "+getFile().getAbsolutePath());
        }
    }

    public DefaultUserPreferences(Properties props) {
        this.props = props;
    }

    private File getFile() {
        String property = System.getProperty(EMF_PREFERENCE);
        if (property != null && new File(property).exists())
            return new File(property);

        return new File(System.getProperty("user.home"),
                gov.epa.emissions.framework.install.installer.Constants.EMF_CLIENT_PREFERENCES_FILE);
    }

    public boolean checkFile(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    private String property(String name) {
        return props.getProperty(name);
    }

    public String inputFolder() {
        return property(LOCAL_INPUT_DRIVE) + property(DEFAULT_INPUT_FOLDER);
    }

    public String outputFolder() {
        return property(LOCAL_OUTPUT_DRIVE) + property(DEFAULT_OUTPUT_FOLDER);
    }

    public String mapLocalInputPathToRemote(String localPath) {
        String local = property(LOCAL_INPUT_DRIVE);
        String remote = property(REMOTE_INPUT_DRIVE);

        String path = remote + localPath.substring(local.length());
        return path.replace('\\', '/');
    }

    public String mapLocalOutputPathToRemote(String localPath) {
        String local = property(LOCAL_OUTPUT_DRIVE);
        String remote = property(REMOTE_OUTPUT_DRIVE);

        String path = remote + localPath.substring(local.length());
        return path.replace('\\', '/');
    }
}
