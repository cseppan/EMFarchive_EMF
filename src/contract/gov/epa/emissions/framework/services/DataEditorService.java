/*
 * Creation on Nov 07, 2005
 * Eclipse Project Name: EMF
 * File Name: DataEditorServices.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;


/**
 * @author Conrad F. D'Cruz
 * 
 */
public interface DataEditorService {
    Page getPage(String tableName, int pageNumber) throws EmfException;
    int getPageCount(String tableName) throws EmfException;
    Page getPageWithRecord(String tableName, int recordId) throws EmfException;
    int getTotalRecords(String tableName) throws EmfException;
    
    Version derive(Version baseVersion) throws EmfException;
    void submit(ChangeSet changeset) throws EmfException;
    
    void close() throws EmfException;
    void markFinal() throws EmfException;
    
}