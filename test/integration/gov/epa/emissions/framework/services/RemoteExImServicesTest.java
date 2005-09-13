package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class RemoteExImServicesTest extends ExImServicesTestCase {

    public RemoteExImServicesTest() throws ConfigurationException {
        super("emf.services.url.remote");
    }

}
