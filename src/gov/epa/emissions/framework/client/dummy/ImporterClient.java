package gov.epa.emissions.framework.client.dummy;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.EMFDataTransport;

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
public class ImporterClient {
    private static String endpoint1 = 
        "http://ben.cep.unc.edu:8080/emf/services/EMFDataService";

    /**
     * @throws EmfException
     * 
     */
    public ImporterClient() throws EmfException {
        super();
        EMFDataTransport emfData1 = new EMFDataTransport(endpoint1);
        emfData1.startImport("FOOBAR_ONE","ORL");
        EMFDataTransport emfData2 = new EMFDataTransport(endpoint1);
        emfData1.startImport("FOOBAR_TWO","IDA");
        
    }

    public static void main(String[] args) throws EmfException {
        new ImporterClient();
    }
}
