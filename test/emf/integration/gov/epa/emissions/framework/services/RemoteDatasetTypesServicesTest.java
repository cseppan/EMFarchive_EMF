package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class RemoteDatasetTypesServicesTest extends DataServicesTestCase {

    public RemoteDatasetTypesServicesTest() throws ConfigurationException {
        super("emf.services.url.remote");
    }

}
