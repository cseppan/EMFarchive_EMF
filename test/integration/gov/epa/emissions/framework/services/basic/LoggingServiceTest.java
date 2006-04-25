package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;
import java.util.Random;

public class LoggingServiceTest extends ServiceTestCase {

    private LoggingServiceImpl logService;

    private DataCommonsService dcService;

    private UserService userService;

    private DataServiceImpl dataService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());

        dcService = new DataCommonsServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
        logService = new LoggingServiceImpl(sessionFactory);
        dataService = new DataServiceImpl(sessionFactory);

    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testVerifyOneAccessLogLogged() throws Exception {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());

        User user = null;
        EmfDataset dataset = null;
        EmfDataset datasetFromDB = null;
        AccessLog alog = null;
        AccessLog returnAlog = null;

        try {
            user = new User("Giovanni Falcone", "UNC", "919-966-9572", "falcone@unc.edu", "falcone" + id, "falcone123",
                    false, false);

            userService.createUser(user);

            dataset = new EmfDataset();
            dataset.setName(user.getUsername() + "_" + id);
            dataset.setAccessedDateTime(new Date());
            dataset.setCreatedDateTime(new Date());
            dataset.setCreator(user.getUsername());
            dataset.setDatasetType(getDatasetType("External File (External)"));
            dataset.setDescription("DESCRIPTION");
            dataset.setModifiedDateTime(new Date());
            dataset.setStartDateTime(new Date());
            dataset.setStatus("imported");
            dataset.setYear(42);
            dataset.setUnits("orl");
            dataset.setTemporalResolution("t1");
            dataset.setStopDateTime(new Date());

            super.add(dataset);

            datasetFromDB = getDataset(dataset);

            alog = new AccessLog(user.getUsername(), datasetFromDB.getId(), new Date(), "0", "description",
                    "folderPath");

            logService.setAccessLog(alog);

            AccessLog[] allLogs = logService.getAccessLogs(datasetFromDB.getId());
            for (int i = 0; i < allLogs.length; i++) {
                returnAlog = allLogs[i];
                assertEquals(returnAlog, alog);
            }
        } finally {
            remove(alog);
            remove(dataset);
            remove(user);
        }
    }

    private EmfDataset getDataset(EmfDataset dataset) throws EmfException {
        EmfDataset datasetFromDB = null;

        EmfDataset[] allDatasets = dataService.getDatasets();

        for (int i = 0; i < allDatasets.length; i++) {
            datasetFromDB = allDatasets[i];

            if (datasetFromDB.getName().equals(dataset.getName()))
                break;
        }

        return datasetFromDB;
    }

    private DatasetType getDatasetType(String string) throws EmfException {
        DatasetType aDST = null;

        DatasetType[] allDST = dcService.getDatasetTypes();
        for (int i = 0; i < allDST.length; i++) {
            aDST = allDST[i];

            if (aDST.getName().equals(string)) {
                break;
            }
        }
        return aDST;
    }

}
