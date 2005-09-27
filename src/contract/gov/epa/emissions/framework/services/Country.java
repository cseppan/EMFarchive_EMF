/*
 * Creation on Sep 27, 2005
 * Eclipse Project Name: EMF
 * File Name: Country.java
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
public class Country implements Serializable {

	private long id;
	private String name;
	
	/**
	 * 
	 */
	public Country() {
		super();
	}

	public Country(String name) {
		super();
		this.name = name;
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

}
