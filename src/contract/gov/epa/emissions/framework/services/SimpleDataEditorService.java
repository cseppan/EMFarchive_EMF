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
public interface SimpleDataEditorService {
    SimplePage getPage(String tableName, int pageNumber) throws EmfException;
    int getPageCount(String tableName) throws EmfException;
    SimplePage getPageWithRecord(String tableName, int recordId) throws EmfException;
    int getTotalRecords(String tableName) throws EmfException;
    void close() throws EmfException;

}