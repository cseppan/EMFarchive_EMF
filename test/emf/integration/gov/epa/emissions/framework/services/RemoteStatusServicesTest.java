/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import junit.framework.TestCase;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class RemoteStatusServicesTest extends StatusServicesTestCase {

    public RemoteStatusServicesTest() {
        super("http://ben.cep.unc.edu:8080/emf/services");
    }

}
