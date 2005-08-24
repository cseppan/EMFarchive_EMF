/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;


/**
 * @author Conrad F. D'Cruz
 * 
 */
public class LocalStatusServicesTest extends StatusServicesTestCase {

    public LocalStatusServicesTest() {
        super("http://localhost:8080/emf/services");
    }

}
