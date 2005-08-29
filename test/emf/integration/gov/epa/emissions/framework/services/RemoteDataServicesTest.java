package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class RemoteDataServicesTest extends DataServicesTestCase {

    public RemoteDataServicesTest() throws ConfigurationException {
        super("emf.services.url.remote");
    }

}
