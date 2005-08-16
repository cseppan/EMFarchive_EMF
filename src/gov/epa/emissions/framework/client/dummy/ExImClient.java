package gov.epa.emissions.framework.client.dummy;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ExImServicesTransport;

/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: Hib
 * Package: 
 * File Name: ImporterClient.java
 * Author: Conrad F. D'Cruz
 */

/**
 * @author Conrad F. D'Cruz
 *
 */
public class ExImClient {
    private static String endpoint1 = 
        "http://ben.cep.unc.edu:8080/emf/services/gov.epa.emf.ExImServices";

    /**
     * @throws EmfException
     * 
     */
    public ExImClient() throws EmfException {
        super();
        System.out.println("START IMPORT CLIENT");
        ExImServicesTransport emfData1 = new ExImServicesTransport(endpoint1);
        emfData1.startImport("aeyth","arinv.nonpoint.nti99_NC.txt","ORL");
        System.out.println("END IMPORT CLIENT");
//        ExImTransport emfData2 = new ExImTransport(endpoint1);
//        emfData1.startImport("cdcruz","FOOBAR_TWO","IDA");
        
    }

    public static void main(String[] args) throws EmfException {
        new ExImClient();
    }
}
