/*
 * Creation on Oct 20, 2005
 * Eclipse Project Name: EMF
 * File Name: TestClient.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.client.transport;

import java.util.List;

public class TestClient {

	private final String url="http://localhost:8080/emf/services/gov.epa.emf.services.TestServices";

	public TestClient() {
		super();
		doStuff();
	}

	private void doStuff() {
		TestServiceTransport tsp = new TestServiceTransport(url);
		List stuff = tsp.getStuff();
		
		System.out.println("Number of elements in the list= " + stuff.size());
	}

	public static void main(String[] args){
		new TestClient();
	}
}
