/*
 * Creation on Nov 16, 2005
 * Eclipse Project Name: EMF
 * File Name: SimpleDataVersioningDAOTest.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.version;

import gov.epa.emissions.framework.InfrastructureException;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class SimpleDataVersioningDAOTest {
    SimpleVersioningDAO sdvDAO = null;

    /**
     * @throws InfrastructureException 
     * 
     */
    public SimpleDataVersioningDAOTest() throws Exception {
        super();
        sdvDAO = new SimpleVersioningDAO();
    }

    
}
