package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

import java.io.File;

public class ControlMeasuresImporterTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }

    public void testShouldImportControlMeasureFiles() throws EmfException, Exception {
        File folder = new File("test/data/cost/controlMeasure");
        String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
        ControlMeasuresImporter importer = new ControlMeasuresImporter(folder, fileNames, sessionFactory());
        importer.run();

        ControlMeasure[] measures = importer.controlMeasures();
        assertEquals(32, measures.length);
        assertEquals(1132, noOfRecords(measures));
        assertEquals(124, noOfScc(measures));
    }

    private int noOfScc(ControlMeasure[] measures) {
        int count = 0;
        for (int i = 0; i < measures.length; i++) {
            count += measures[i].getSccs().length;
        }
        return count;
    }

    private int noOfRecords(ControlMeasure[] measures) {
        int count = 0;
        for (int i = 0; i < measures.length; i++) {
            count += measures[i].getEfficiencyRecords().length;
        }
        return count;
    }

}
