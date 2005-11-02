/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;

public class InterDataServicesTest extends WebServicesIntegrationTestCase {
    protected InterDataServices services;

    private EmfDataset dataset;

    protected void setUp() {
        services = serviceLocator.getInterDataServices();
    }

    protected void tearDown() throws Exception {
    }

    public void testFetchEmfKeywords() throws EmfException {
        Keyword[] emfKeywords = services.getKeywords();
        assertTrue("Should have atleast 1 keyword", emfKeywords.length > 0);
    }

}
