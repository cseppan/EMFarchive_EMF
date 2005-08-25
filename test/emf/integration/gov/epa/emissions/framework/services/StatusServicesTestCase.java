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
public abstract class StatusServicesTestCase extends ServicesTestCase {

    private StatusServices service;

    public StatusServicesTestCase(String baseUrlProperty) throws ConfigurationException {
        super(baseUrlProperty);

        ServiceLocator locator = new RemoteServiceLocator(super.baseUrl);
        service = locator.getStatusServices();
    }

    public void testInsert() throws EmfException {
        Status aStat = new Status();
        aStat.setMessage("import started for file XYZABC");
        aStat.setMessageType("INFOMATICA");
        aStat.setTimestamp(new Date());
        aStat.setUserName("cdcruz");

        service.setStatus(aStat);
    }

    public void testGet() throws EmfException {
        Status[] stats = service.getMessages("emf");
        // FIXME: insert first, before assertion
        System.out.println("Total number of status messages: " + stats.length);

        for (int i = 0; i < stats.length; i++) {
            Status aStat = stats[i];
            System.out.println("" + aStat.getUserName());
            System.out.println("" + aStat.getTimestamp());
            System.out.println("" + aStat.getMessageType());
            System.out.println("" + aStat.getMessage());
            System.out.println("" + aStat.isMsgRead());
        }

    }

}
