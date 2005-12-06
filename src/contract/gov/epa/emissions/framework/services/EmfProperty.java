package gov.epa.emissions.framework.services;

import java.io.Serializable;

/**
 * This is a value object that holds an EMF property
 * 
 * @author Conrad F. D'Cruz
 */
public class EmfProperty implements Serializable {

    private long propertyid;

    private String propertyname = null;

    private String propertyvalue = null;

    public EmfProperty() {
        // The default no-argument constructor needed
        // by hibernate and axis
    }

    public EmfProperty(String name, String value) {
        super();
        setPropertyname(name);
        setPropertyvalue(value);
    }

    public long getPropertyid() {
        return propertyid;
    }

    public void setPropertyid(long propertyid) {
        this.propertyid = propertyid;
    }

    public String getPropertyname() {
        return propertyname;
    }

    public void setPropertyname(String propertyname) {
        this.propertyname = propertyname;
    }

    public String getPropertyvalue() {
        return propertyvalue;
    }

    public void setPropertyvalue(String propertyvalue) {
        this.propertyvalue = propertyvalue;
    }

}
