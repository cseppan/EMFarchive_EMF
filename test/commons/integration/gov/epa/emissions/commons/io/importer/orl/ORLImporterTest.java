package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.CommonsTestCase;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.ORLTableType;

import java.io.File;

public class ORLImporterTest extends CommonsTestCase {

    public void testNonPoint() throws Exception {
        doImport("arinv.nonpoint.nti99_NC.txt", DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
                ORLTableType.ORL_AREA_NONPOINT_TOXICS);
    }

    public void testNonRoad() throws Exception {
        doImport("arinv.nonroad.nti99d_NC.new.txt", DatasetTypes.ORL_AREA_NONROAD_TOXICS,
                ORLTableType.ORL_AREA_NONROAD_TOXICS);
    }

    public void testPoint() throws Exception {
        doImport("ptinv.nti99_NC.txt", DatasetTypes.ORL_POINT_TOXICS, ORLTableType.ORL_POINT_TOXICS);
    }

    public void testOnRoadMobile() throws Exception {
        doImport("nti99.NC.onroad.SMOKE.txt", DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS,
                ORLTableType.ORL_ONROAD_MOBILE_TOXICS);
    }

    private void doImport(final String filename, String datasetType, ORLTableType tableType) throws Exception {
        String table = filename.substring(0, filename.length() - 4).replace('.', '_');

        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        // only one base type
        dataset.addTable(new Table(tableType.baseTypes()[0], table));
        dataset.addTable(new Table(tableType.summaryType(), table + "_summary"));

        ORLImporter importer = new ORLImporter(dbSetup.getDbServer(), false, true);
        importer.run(new File[] { new File("test/commons/data/orl/nc", filename) }, dataset, true);
    }

}
