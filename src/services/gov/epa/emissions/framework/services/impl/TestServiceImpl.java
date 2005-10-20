/*
 * Creation on Oct 20, 2005
 * Eclipse Project Name: EMF
 * File Name: TestServiceImpl.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.TestService;
import gov.epa.emissions.framework.services.TestStuff;

import java.util.ArrayList;
import java.util.List;

public class TestServiceImpl implements TestService {

	public TestServiceImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public List getStuff(){
		ArrayList allStuff = new ArrayList();
		TestStuff tst = null;
		
		tst = new TestStuff(1,"a","b");
		allStuff.add(tst);
		tst = new TestStuff(2,"c","d");
		allStuff.add(tst);
		tst = new TestStuff(3,"e","f");
		allStuff.add(tst);
		
		return allStuff;
	}

}
