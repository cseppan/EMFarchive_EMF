/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import org.apache.commons.configuration.ConfigurationException;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class RemoteLoggingServicesTest extends StatusServicesTestCase {

    public RemoteLoggingServicesTest() throws ConfigurationException {
        super("emf.services.url.remote");
    }

}
