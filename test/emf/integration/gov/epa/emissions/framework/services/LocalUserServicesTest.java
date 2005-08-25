package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class LocalUserServicesTest extends UserServicesTestCase {

    public LocalUserServicesTest() throws ConfigurationException {
        super("emf.services.url.local");
    }

}
