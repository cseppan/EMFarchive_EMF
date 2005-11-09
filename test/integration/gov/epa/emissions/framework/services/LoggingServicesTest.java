/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.PostgresDbUpdate;

import java.util.Date;

public class LoggingServicesTest extends WebServicesIntegrationTestCase {

    private LoggingServices service;

    protected void setUp() throws Exception {
        ServiceLocator locator = new RemoteServiceLocator(super.baseUrl);
        service = locator.getLoggingServices();
    }

    public void testInsert() throws Exception {
        AccessLog log = new AccessLog();
        int datasetId = 1;
        log.setDatasetid(datasetId);
        log.setDescription("FOO BAR");
        log.setFolderPath("somepath");
        log.setTimestamp(new Date());
        log.setUsername("jbond");
        log.setVersion("v1");

        try {
            service.setAccessLog(log);
        } finally {
            new PostgresDbUpdate().delete("emf.dataset_access_logs", "dataset_id", datasetId);
        }
    }

}
