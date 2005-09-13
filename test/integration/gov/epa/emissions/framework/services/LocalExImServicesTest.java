package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class LocalExImServicesTest extends ExImServicesTestCase {

    public LocalExImServicesTest() throws ConfigurationException {
        super("emf.services.url.local");
    }

}
