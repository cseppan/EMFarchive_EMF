/*
 * Created on Jul 28, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: EMFData.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 *
 */
public interface StatusServices {

    public void setStatus(Status status) throws EmfException;
    public Status[] getMessages(String userName) throws EmfException;
    public Status[] getMessages(String userName, String type) throws EmfException;
}
