package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import junit.framework.TestCase;

public abstract class ServicesTestCase extends TestCase {
    protected String baseUrl;

    protected ServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        CompositeConfiguration config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());

        String configFile = "test/integration/integration.conf";
        try {
            PropertiesConfiguration propertiesConfig = new PropertiesConfiguration(configFile);
            config.addConfiguration(propertiesConfig);
        } catch (ConfigurationException e) {
            System.err.println("Please ensure that your configuration is defined in " + configFile
                    + ". If not present, copy the TEMPLATE* file as integration.conf and rerun the test");
            throw e;
        }

        this.baseUrl = config.getString(baseUrlProperty);
    }
}
