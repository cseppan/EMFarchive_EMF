/*
 * Created on Jul 20, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: EMFUser.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.commons;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFUser {

    String name = null;
    
    /**
     * 
     */
    public EMFUser() {
        super();
    }

    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
}
