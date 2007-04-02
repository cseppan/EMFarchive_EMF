package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.io.File;

public class CMImportTaskTest extends ServiceTestCase {

    private ControlMeasureDAO dao;

    public CMImportTaskTest() {
        this.dao = new ControlMeasureDAO();
    }

    protected void doSetUp() throws Exception {
        //
    }

    protected void doTearDown() throws Exception {
        //
    }

    public void testShouldImportControlMeasureFiles() throws Exception {
        try {
            File folder = new File("test/data/cost/controlMeasure");
            String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
            CMImportTask task = new CMImportTask(folder, fileNames, emfUser(), sessionFactory(), dbServer());
            task.run();
            ControlMeasure[] measures = measures();
            assertEquals(32, measures.length);
            assertEquals(1132, noOfRecords(measures));
            assertEquals(126, noOfScc());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            dropAll(Scc.class);
            dropAll(EfficiencyRecord.class);
            dropAll(ControlMeasure.class);
            dropAll(Status.class);
        }
    }
    
    public void testShouldImportControlMeasureFilesTwice() throws Exception {
        try {
            File folder = new File("test/data/cost/controlMeasure");
            String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
            CMImportTask task = new CMImportTask(folder, fileNames, emfUser(), sessionFactory(), dbServer());
            task.run();
            ControlMeasure[] measures = measures();
            assertEquals(32, measures.length);
            assertEquals(1132, noOfRecords(measures));
            assertEquals(126, noOfScc());
            
            task.run();
            
            measures = measures();
            assertEquals(32, measures.length);
            assertEquals(1132, noOfRecords(measures));
            assertEquals(126, noOfScc());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            dropAll(Scc.class);
            dropAll(EfficiencyRecord.class);
            dropAll(ControlMeasure.class);
            dropAll(Status.class);
        }
    }

    private ControlMeasure[] measures() {
        return (ControlMeasure[]) dao.all(session).toArray(new ControlMeasure[0]);
    }

    private User emfUser() {
        return new UserDAO().get("emf", session);
    }

    private int noOfScc() {
        HibernateFacade facade = new HibernateFacade();
        return facade.getAll(Scc.class, session).size();
    }

    private int noOfRecords(ControlMeasure[] measures) {
        int count = 0;
        for (int i = 0; i < measures.length; i++) {
            count += dao.getEfficiencyRecords(measures[i].getId(), session).size();
        }
        return count;
    }

}
