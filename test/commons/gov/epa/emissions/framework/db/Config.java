package gov.epa.emissions.framework.db;


import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public class Config {

    private CompositeConfiguration config;

    public Config(String file) throws ConfigurationException {
        config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());

        try {
            PropertiesConfiguration propertiesConfig = new PropertiesConfiguration(file);
            config.addConfiguration(propertiesConfig);
        } catch (ConfigurationException e) {
            System.err.println("Please ensure that your configuration is defined in " + file
                    + ". If not present, copy the TEMPLATE-" + file + " as " + file
                    + ", customize (as needed), and rerun the test");
            throw e;
        }
    }

    public String driver() {
        return "org.postgresql.Driver";
    }

    public String url() {
        return "jdbc:postgresql://" + value("database.host") + "/" + value("database.name");
    }

    private String value(String name) {
        return (String) config.getProperty(name);
    }

    public String username() {
        return value("database.username");
    }

    public String password() {
        return value("database.password");
    }
}
