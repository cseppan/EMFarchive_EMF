package gov.epa.emissions.framework.services;


import org.hibernate.Session;

public interface EmfProperties {

    EmfProperty getProperty(String name, Session session);

}