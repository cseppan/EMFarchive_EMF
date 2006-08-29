package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import java.io.File;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class CMImportTaskTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub

    }

    protected void doTearDown() throws Exception {
        //
    }

    public void testShouldImportControlMeasureFiles() throws Exception {
        //ControlMeasure[] measures = null;
        try {
            File folder = new File("test/data/cost/controlMeasure");
            String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
            //CMImportTask task = 
            new CMImportTask(folder, fileNames, emfUser(), sessionFactory());
            //measures = task.run();
            //assertEquals(32, measures.length);
            //assertEquals(1132, noOfRecords(measures));
            //assertEquals(124, noOfScc(measures));
        } finally {
            dropAll(ControlMeasure.class);
            dropAll(Status.class);
        }
    }

    private User emfUser() {
        return new UserDAO().get("emf", session);
    }

    // private int noOfScc(ControlMeasure[] measures) {
    // int count = 0;
    // for (int i = 0; i < measures.length; i++) {
    // count += measures[i].getSccs().length;
    // }
    // return count;
    // }
    //
    // private int noOfRecords(ControlMeasure[] measures) {
    // int count = 0;
    // for (int i = 0; i < measures.length; i++) {
    // count += measures[i].getEfficiencyRecords().length;
    //        }
    //        return count;
    //    }

}
