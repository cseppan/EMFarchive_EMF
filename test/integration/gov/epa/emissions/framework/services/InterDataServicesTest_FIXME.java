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

public abstract class InterDataServicesTest_FIXME extends WebServicesIntegrationTestCase {
    protected InterDataServices services;

    protected void setUp() {
        services = serviceLocator.getInterDataServices();
    }

    public void testFetchEmfKeywords() throws EmfException {
        services.insertKeyword(new Keyword("1"));
        Keyword[] keywords = services.getKeywords();
        assertTrue("Should have atleast 1 keyword", keywords.length > 0);
        services.deleteKeyword(keywords[0]);
    }

}
