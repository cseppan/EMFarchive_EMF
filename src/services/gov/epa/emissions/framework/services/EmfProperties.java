package gov.epa.emissions.framework.services;


import gov.epa.emissions.framework.services.basic.EmfProperty;

import org.hibernate.Session;

public interface EmfProperties {

    EmfProperty getProperty(String name, Session session);

}