package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.CommonsTestCase;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.ORLTableType;
import gov.epa.emissions.commons.io.importer.orl.ORLImporter;

import java.io.File;

public class ORLExporterTest extends CommonsTestCase {

    public void testPoint() throws Exception {
        doImport("ptinv.nti99_NC.txt", DatasetTypes.ORL_POINT_TOXICS, ORLTableType.ORL_POINT_TOXICS);
        doExport(DatasetTypes.ORL_POINT_TOXICS, ORLTableType.ORL_POINT_TOXICS, "ptinv_nti99_NC");
    }

    public void testNonPoint() throws Exception {
        doImport("arinv.nonpoint.nti99_NC.txt", DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
                ORLTableType.ORL_AREA_NONPOINT_TOXICS);
        doExport(DatasetTypes.ORL_AREA_NONPOINT_TOXICS, ORLTableType.ORL_AREA_NONPOINT_TOXICS, "arinv_nonpoint_nti99_NC");
    }

    public void testOnRoadMobile() throws Exception {
        doImport("nti99.NC.onroad.SMOKE.txt", DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS,
                ORLTableType.ORL_ONROAD_MOBILE_TOXICS);
        doExport(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS, ORLTableType.ORL_ONROAD_MOBILE_TOXICS, "nti99_NC_onroad_SMOKE");
    }

    public void testNonRoad() throws Exception {
        doImport("arinv.nonroad.nti99d_NC.new.txt", DatasetTypes.ORL_AREA_NONROAD_TOXICS,
                ORLTableType.ORL_AREA_NONROAD_TOXICS);
        doExport(DatasetTypes.ORL_AREA_NONROAD_TOXICS, ORLTableType.ORL_AREA_NONROAD_TOXICS, "arinv_nonroad_nti99d_NC_new");
    }

    private void doExport(String datasetType, ORLTableType tableType, String tableName) throws Exception {
        Dataset dataset = createDataset(datasetType, tableType, tableName);

        ORLExporter exporter = new ORLExporter(dbSetup.getDbServer());

        String tempDir = System.getProperty("java.io.tmpdir");
        String exportFileName = tempDir + "/" + datasetType + "." + tableName + ".EXPORTED_";

        // ORL has only one base table type
        exporter.exportTableToFile(tableType.baseTypes()[0], dataset, exportFileName);
    }

    private Dataset createDataset(String datasetType, ORLTableType tableType, String tableName) {
        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        // only one base type
        dataset.addTable(new Table(tableType.baseTypes()[0], tableName));
        dataset.setRegion("US");
        dataset.setCountry("US");
        dataset.setYear(1234);
        dataset.setDescription("This is the first line of an artificial description\nThis is the second line");

        return dataset;
    }

    private void doImport(final String filename, String datasetType, ORLTableType tableType) throws Exception {
        String tableName = filename.substring(0, filename.length() - 4).replace('.', '_');

        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        // only one base type
        dataset.addTable(new Table(tableType.baseTypes()[0], tableName));
        dataset.addTable(new Table(tableType.summaryType(), tableName + "_summary"));

        ORLImporter importer = new ORLImporter(dbSetup.getDbServer(), false, true);
        importer.run(new File[] { new File("test/commons/data/orl/nc", filename) }, dataset, true);
    }
}
