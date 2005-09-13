package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class RemoteUserServicesTest extends UserServicesTestCase {

    public RemoteUserServicesTest() throws ConfigurationException {
        super("emf.services.url.remote");
    }

}
