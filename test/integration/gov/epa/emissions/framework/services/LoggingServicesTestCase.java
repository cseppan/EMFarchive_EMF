/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

import java.util.Date;

import org.apache.commons.configuration.ConfigurationException;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public abstract class LoggingServicesTestCase extends ServicesTestCase {

    private LoggingServices service;

    public LoggingServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);

    }

    protected void setUp() {
        ServiceLocator locator = new RemoteServiceLocator(super.baseUrl);
        service = locator.getLoggingServices();
    }

    
    public void hld_testInsert() throws EmfException {
    	AccessLog al = new AccessLog();
    	al.setDatasetid(1);
    	al.setDescription("FOO BAR");
    	al.setFolderPath("somepath");
    	al.setTimestamp(new Date());
    	al.setUsername("jbond");
    	al.setVersion("v1");		

        service.setAccessLog(al);
    }

}
