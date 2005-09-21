/*
 * Created on Aug 11, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: ServiceLocator.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.UserServices;

// TODO: find a better package location for locator classes
public class RemoteServiceLocator implements ServiceLocator {

    private String baseUrl;

    public RemoteServiceLocator(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public UserServices getUserServices() {

        return new UserServicesTransport(baseUrl + "/gov.epa.emf.services.UserServices");
    }

    public StatusServices getStatusServices() {
        return new StatusServicesTransport(baseUrl + "/gov.epa.emf.services.StatusServices");
    }

    public ExImServices getExImServices() {
        return new ExImServicesTransport(baseUrl + "/gov.epa.emf.services.ExImServices");
    }

    public DataServices getDataServices() {
        return new DataServicesTransport(baseUrl + "/gov.epa.emf.services.DataServices");
    }

    public LoggingServices getLoggingServices() {
        return new LoggingServicesTransport(baseUrl + "/gov.epa.emf.services.LoggingServices");
    }
    
}
