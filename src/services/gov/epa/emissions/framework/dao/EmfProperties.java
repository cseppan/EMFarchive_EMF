package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.impl.EmfProperty;

import org.hibernate.Session;

public interface EmfProperties {

    EmfProperty getProperty(String name, Session session);

}