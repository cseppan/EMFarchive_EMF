package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class LocalDataServicesTest extends DataServicesTestCase {

    public LocalDataServicesTest() throws ConfigurationException {
        super("emf.services.url.local");
    }

}
