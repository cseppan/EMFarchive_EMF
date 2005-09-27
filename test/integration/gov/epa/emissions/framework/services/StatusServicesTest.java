/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.db.DbUpdate;

import java.util.Date;

public class StatusServicesTest extends ServicesTestCase {

    private StatusServices service;

    protected void setUp() {
        service = super.serviceLocator.getStatusServices();
    }

    public void testInsert() throws Exception {
        Status status = new Status();
        status.setMessage("import started for file XYZABC");
        status.setMessageType("INFOMATICA");
        status.setTimestamp(new Date());
        String username = "cdcruz";
        status.setUsername(username);

        try {
            service.setStatus(status);
        } finally {
            new DbUpdate().deleteAll("emf.statusmessages");
        }
    }

}
