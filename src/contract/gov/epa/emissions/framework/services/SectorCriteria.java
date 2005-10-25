/*
 * Creation on Oct 25, 2005
 * Eclipse Project Name: EMF
 * File Name: SectorCriteria.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services;

import java.io.Serializable;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class SectorCriteria implements Serializable {

    private long id;
    private String type;
    private String criteria;
    
    /**
     * @return Returns the criteria.
     */
    public String getCriteria() {
        return criteria;
    }

    /**
     * @param criteria The criteria to set.
     */
    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    /**
     * @return Returns the id.
     */
    public long getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     */
    public SectorCriteria() {
        super();
    }

}
