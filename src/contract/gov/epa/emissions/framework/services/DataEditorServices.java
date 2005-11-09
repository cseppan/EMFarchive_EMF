/*
 * Creation on Nov 07, 2005
 * Eclipse Project Name: EMF
 * File Name: DataEditorServices.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public interface DataEditorServices {
    String getName() throws EmfException;

    void setName(String name) throws EmfException;

    Page getPage(String tableName, int pageNumber) throws EmfException;

    int getPageCount(String tableName) throws EmfException;
}