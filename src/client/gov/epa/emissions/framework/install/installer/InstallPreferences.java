package gov.epa.emissions.framework.install.installer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class InstallPreferences {

    private static final String DEFAULT_INPUT_FOLDER = "default.input.folder";

    private static final String DEFAULT_OUTPUT_FOLDER = "default.output.folder";

    private static final String LOCAL_OUTPUT_DRIVE = "local.output.drive";

    private static final String LOCAL_INPUT_DRIVE = "local.input.drive";
    
    private static final String EMF_INSTALL_FOLDER = "emf.install.folder";
    
    private static final String WEB_SITE = "web.site";

    public static final String EMF_PREFERENCE = "EMF_PREFERENCE";

    private Properties props;

    public InstallPreferences() throws Exception {
        props = new Properties();
        try {
            FileInputStream inStream = new FileInputStream(getFile());
            props.load(inStream);
        } catch (Exception e) {
            throw new Exception("Cannot load user preferences file");
        }
    }

    public InstallPreferences(Properties props) {
        this.props = props;
    }

    private File getFile() {
        String property = System.getProperty(EMF_PREFERENCE);
        if (property != null && new File(property).exists())
            return new File(property);

        return new File(System.getProperty("user.home"), Generic.USER_PARAMETER);
    }

    public boolean checkFile(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    private String property(String name) {
        return props.getProperty(name);
    }

    public String inputFolder() {
        return property(LOCAL_INPUT_DRIVE) + "\\:" + 
            property(DEFAULT_INPUT_FOLDER).replace('/', '\\');
    }

    public String outputFolder() {
        return property(LOCAL_OUTPUT_DRIVE) + "\\:" +
            property(DEFAULT_OUTPUT_FOLDER).replace('/', '\\');
    }
    
    public String emfInstallFolder() {
        return property(EMF_INSTALL_FOLDER).replace('/', '\\');
    }
    
    public String emfWebSite() {
        return property(WEB_SITE);
    }
 
}
