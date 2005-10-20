/*
 * Creation on Oct 20, 2005
 * Eclipse Project Name: EMF
 * File Name: TestStuff.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services;

import java.io.Serializable;

public class TestStuff implements Serializable {

	private String name;
	private int age;
	private String description;
	
	/**
	 * @param age
	 * @param description
	 * @param name
	 */
	public TestStuff(int age, String description, String name) {
		super();
		this.age = age;
		this.description = description;
		this.name = name;
	}

	/**
	 * @return Returns the age.
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @param age The age to set.
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
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

	public TestStuff() {
		super();
	}

}
