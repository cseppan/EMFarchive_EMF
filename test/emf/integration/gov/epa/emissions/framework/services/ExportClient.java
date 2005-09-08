/*
 * Creation on Sep 2, 2005
 * Eclipse Project Name: EMF
 * File Name: ExportClient.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.ORLTableTypes;
import gov.epa.emissions.commons.io.importer.TableType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.ExImServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExportClient {
    // http://localhost:8080/emf/services/gov.epa.emf.services.DataServices

    private static Log log = LogFactory.getLog(ExportClient.class);

    private RemoteServiceLocator svcLoc = null;

    private ExImServices eximSvc = null;

    private UserServices userSvc = null;

    public ExportClient() {
        super();
        log.debug("IN CONSTRUCTOR");
        try {
            svcLoc = new RemoteServiceLocator("http://localhost:8080/emf/services");
            eximSvc = svcLoc.getExImServices();
            userSvc = svcLoc.getUserServices();
            // User user = userSvc.getUser("admin");
            // System.out.println(user.getUserName());
            // doStuff();
            exportDataset();
        } catch (EmfException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.debug("END CONSTRUCTOR");
    }

    private void exportDataset() throws EmfException, IOException, InterruptedException {
        DatasetType datasetType = new DatasetType();
        datasetType.setName(DatasetTypes.ORL_AREA_NONPOINT_TOXICS);
        User user = userSvc.getUser("emf");

        System.out.println("$$$$$$$$$$$$$$$$ User object " + user.getUserName());

//        File userDir = new File(System.getProperty("user.dir"));
//        File file = new File(userDir, "test/commons/data/orl/nc/arinv.nonpoint.nti99_NC.txt");
        File userDir = new File(System.getProperty("user.dir"));
        String pathToFile="test\\commons\\data\\orl\\nc";
        File repository = new File(userDir,pathToFile);

        // EmfDataset dataset = new EmfDataset();
        EmfDataset dataset = createDataset(DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
                ORLTableTypes.ORL_AREA_NONPOINT_TOXICS, "arinv_nonpoint_nti99_NC");

        dataset.setCreator(user.getFullName());
        dataset.setName("ORL NonPoint Conrad");
        dataset.setDatasetType(datasetType.getName());

//        eximSvc.startImport(user, file.getPath(), dataset, datasetType);
        eximSvc.startImport(user, repository.getAbsolutePath(), "arinv.nonpoint.nti99_NC.txt", dataset, datasetType);

        System.out.println(new Date());
        for (int i = 0; i < 10000; i++) {
            synchronized (this) {
                wait(1);
            }
        }
        System.out.println(new Date());
        File fileOut = new File(System.getProperty("user.dir"), "/test/commons/data/orl/nc/output/orlnonpoint.txt");
        eximSvc.startExport(user, new EmfDataset[]{dataset}, fileOut.getPath());
        System.out.println("Export ended: " + fileOut.getPath());
    }

    private EmfDataset createDataset(String datasetType, TableType tableType, String tableName) {
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        // only one base type
        dataset.addTable(new Table(tableType.baseTypes()[0], tableName));
        dataset.setRegion("US");
        dataset.setCountry("US");
        dataset.setYear(1234);
        dataset.setDescription("This is the first line of an artificial description\nThis is the second line");

        return dataset;
    }

    public static void main(String[] args) throws Exception {
        new ExportClient();
    }

}
