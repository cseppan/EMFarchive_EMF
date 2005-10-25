/*
 * Creation on Sep 27, 2005
 * Eclipse Project Name: EMF
 * File Name: Sector.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * This class holds the name and description of an EMF Sector
 * 
 * @author Conrad F. D'Cruz
 *
 */
public class Sector implements Serializable {
	private long id;
	private String name;
	private String description;
    private List sectorCriteria;
	
	/**
	 * 
	 */
	public Sector() {
		super();
	}

	public Sector(String description, String name) {
		super();
		this.description = description;
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    /**
     * @return Returns the sectorCriteria.
     */
    public SectorCriteria[] getSectorCriteria() {
        return (SectorCriteria[])this.sectorCriteria.toArray(new SectorCriteria[0]);
    }

    /**
     * @param sectorCriteria The sectorCriteria to set.
     */
    public void setSectorCriteria(SectorCriteria[] sectorCriteria) {
        this.sectorCriteria.clear();
        this.sectorCriteria.addAll(Arrays.asList(sectorCriteria));
    }

    public void addSectorCriteria(SectorCriteria criteria) {
        this.sectorCriteria.add(criteria);
    }

}
