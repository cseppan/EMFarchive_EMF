package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.TableTypes;

import java.io.File;

public class ORLImporterTest extends EmissionsDataSetupTestCase {

    public void testNonPointUsingPostgres() throws Exception {
        run("arinv.nonpoint.nti99_NC.txt", DatasetTypes.ORL_AREA_NONPOINT_TOXICS, TableTypes.ORL_AREA_NONPOINT_TOXICS);
    }

    // FIXME: change the columns from numbers to prefix alphabets
    public void testNonRoad() throws Exception {
        run("arinv.nonroad.nti99d_NC.new.txt", DatasetTypes.ORL_AREA_NONROAD_TOXICS, TableTypes.ORL_AREA_NONROAD_TOXICS);
    }

    public void TODO_testPoint() {
        // String datasetType = DatasetTypes.ORL_POINT_TOXICS;
        // String tableType = TableTypes.ORL_POINT_TOXICS;
        // String dbFile = "ptinv.nti99_NC.100.txt";
    }

    public void testOnRoadMobile() throws Exception {
        run("nti99.NC.onroad.SMOKE.txt", DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS, TableTypes.ORL_MOBILE_TOXICS);
    }

    private void run(final String filename, String datasetType, String tableType) throws Exception {
        String table = filename.substring(0, filename.length() - 4).replace('.', '_');

        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        dataset.addDataTable(tableType, table);
        String summaryTableType = DatasetTypes.getSummaryTableType(datasetType);
        dataset.addDataTable(summaryTableType, table + "_summary");

        ORLImporter importer = new ORLImporter(dbSetup.getDbServer(), false, true);
        importer.run(new File[] { new File("test/commons/data/orl/nc", filename) }, dataset, true);
    }

}
