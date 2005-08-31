package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.CommonsTestCase;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.ORLTableTypes;
import gov.epa.emissions.commons.io.importer.TableType;

public abstract class ORLImporterTestCase extends CommonsTestCase {

    abstract protected void doImport(String filename, Dataset dataset) throws Exception;

    public void testNonPoint() throws Exception {
        run("arinv.nonpoint.nti99_NC.txt", DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
                ORLTableTypes.ORL_AREA_NONPOINT_TOXICS);
    }

    private void run(String filename, String datasetType, TableType tableType) throws Exception {
        String table = filename.substring(0, filename.length() - 4).replace('.', '_');

        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        // only one base type
        dataset.addTable(new Table(tableType.baseTypes()[0], table));
        dataset.addTable(new Table(tableType.summaryType(), table + "_summary"));

        doImport(filename, dataset);
    }

    public void testNonRoad() throws Exception {
        run("arinv.nonroad.nti99d_NC.new.txt", DatasetTypes.ORL_AREA_NONROAD_TOXICS,
                ORLTableTypes.ORL_AREA_NONROAD_TOXICS);
    }

    public void testPoint() throws Exception {
        run("ptinv.nti99_NC.txt", DatasetTypes.ORL_POINT_TOXICS, ORLTableTypes.ORL_POINT_TOXICS);
    }

    public void testOnRoadMobile() throws Exception {
        run("nti99.NC.onroad.SMOKE.txt", DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS, ORLTableTypes.ORL_ONROAD_MOBILE_TOXICS);
    }

}
