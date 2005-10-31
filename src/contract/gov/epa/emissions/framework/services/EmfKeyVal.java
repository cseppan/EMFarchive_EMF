/*
 * Creation on Oct 26, 2005
 * Eclipse Project Name: EMF
 * File Name: EmfKeyword.java
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
public class EmfKeyVal implements Serializable {

    private long id;
    private String keyword;
    private String value;
    
    /**
     * 
     */
    public EmfKeyVal() {
        super();
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
     * @return Returns the keyword.
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword The keyword to set.
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

}
