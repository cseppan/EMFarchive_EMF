package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;

public abstract class DataServicesTestCase extends ServicesTestCase {

    protected DataServices dataService;

    protected DataServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);
    }

    protected void setUp() {
        RemoteServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        dataService = serviceLocator.getDataServices();
    }

public void test(){}


}
