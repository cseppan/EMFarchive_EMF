/*
 * Creation on Nov 07, 2005
 * Eclipse Project Name: EMF
 * File Name: DataEditorServices.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;


/**
 * @author Conrad F. D'Cruz
 * 
 */
public interface DataEditorServices {
    String getName() throws Exception;
    void setName(String name) throws Exception;	
    
    Page getPage(String tableName, int pageNumber) throws Exception;
    int getPageCount(String tableName) throws Exception;
}