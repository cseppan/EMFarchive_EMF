/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DataServices.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public interface KeywordServices {

    EmfKeyword[] getEmfKeywords() throws EmfException;
    void deleteEmfKeyword(EmfKeyword keyword) throws EmfException;
    void insertEmfKeyword(EmfKeyword keyword) throws EmfException;
	void updateEmfKeyword(EmfKeyword keyword) throws EmfException;
}