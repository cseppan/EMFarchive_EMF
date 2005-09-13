package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

public class LocalDatasetTypesServicesTest extends DatasetTypesServicesTestCase {

    public LocalDatasetTypesServicesTest() throws ConfigurationException {
        super("emf.services.url.local");
    }

}
