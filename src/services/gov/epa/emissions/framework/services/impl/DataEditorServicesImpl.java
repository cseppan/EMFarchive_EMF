/*
 * Creation on Nov 7, 2005
 * Eclipse Project Name: EMF
 * File Name: DataEditorServicesImpl.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.epa.emissions.framework.services.DataEditorServices;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class DataEditorServicesImpl implements DataEditorServices {
    private static Log log = LogFactory.getLog(DataEditorServicesImpl.class);

    private String name="Default";
    /**
     * 
     */
    public DataEditorServicesImpl() {
        super();
        log.debug("CONSTRUCTOR HASHCODE: " + this.hashCode());
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.services.DataEditorServices#getName()
     */
    public String getName() {
        log.debug("GET NAME HASHCODE: " + this.hashCode());
        return this.name;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.services.DataEditorServices#setName(java.lang.String)
     */
    public void setName(String name) {
        log.debug("SETNAME HASHCODE: " + this.hashCode() +" name= " + name);
        this.name=name;
    }

}
