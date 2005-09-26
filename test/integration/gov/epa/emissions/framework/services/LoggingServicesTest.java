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

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class LoggingServicesTest extends ServicesTestCase {

    private LoggingServices service;

    protected void setUp() {
        ServiceLocator locator = new RemoteServiceLocator(super.baseUrl);
        service = locator.getLoggingServices();
    }

    public void testInsert() throws EmfException {
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
