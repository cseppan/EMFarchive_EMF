/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.PostgresDbUpdate;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.sql.SQLException;
import java.util.Date;

import org.dbunit.DatabaseUnitException;

public class StatusServiceTest extends ServicesTestCase {

    private StatusService service;

    private PostgresDbUpdate dbUpdate;

    protected void setUp() throws Exception {
        super.setUp();

        ServiceLocator serviceLocator = serviceLocator();
        service = serviceLocator.getStatusService();
        dbUpdate = new PostgresDbUpdate();
        clean();
    }

    protected void tearDown() throws Exception {
        clean();
    }

    public void testCreate() throws Exception {
        Status status = new Status();
        status.setMessage("import started for file XYZABC");
        status.setMessageType("INFOMATICA");
        status.setTimestamp(new Date());
        String username = "test-user";
        status.setUsername(username);

        service.create(status);
        assertEquals(1, service.getAll("test-user").length);
    }

    private void clean() throws DatabaseUnitException, SQLException {
        dbUpdate.deleteAll("emf.statusmessages");
    }

}
