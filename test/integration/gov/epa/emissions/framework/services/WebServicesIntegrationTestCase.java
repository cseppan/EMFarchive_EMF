package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import junit.framework.TestCase;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public abstract class WebServicesIntegrationTestCase extends TestCase {
    protected String baseUrl;

    protected ServiceLocator serviceLocator = null;

    public WebServicesIntegrationTestCase() {
        CompositeConfiguration config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());

        String configFile = "test/integration/integration.conf";
        try {
            PropertiesConfiguration propertiesConfig = new PropertiesConfiguration(configFile);
            config.addConfiguration(propertiesConfig);
        } catch (ConfigurationException e) {
            System.err.println("Please ensure that your configuration is defined in " + configFile
                    + ". If not present, copy the TEMPLATE* file as integration.conf and rerun the test");
            throw new RuntimeException("could not read config file - " + configFile);
        }

        this.baseUrl = config.getString("emf.services.url");
        try {
            this.serviceLocator = new RemoteServiceLocator(baseUrl);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
