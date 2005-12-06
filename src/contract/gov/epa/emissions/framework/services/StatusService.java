package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public interface StatusService extends EMFService {

    // FIXME: Use User object instead of username
    Status[] getAll(String username) throws EmfException;
}
