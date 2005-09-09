package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.CommonsTestCase;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ORLTableTypes;
import gov.epa.emissions.commons.io.importer.TableType;
import gov.epa.emissions.commons.io.importer.orl.BaseORLImporter;

import java.io.File;

public class ORLExporterTest extends CommonsTestCase {

    public void testPoint() throws Exception {
        doImport("ptinv.nti99_NC.txt", DatasetTypes.ORL_POINT_TOXICS, ORLTableTypes.ORL_POINT_TOXICS);

        String datasetType = DatasetTypes.ORL_POINT_TOXICS;
        String tableName = "ptinv_nti99_NC";
        File file = createFile(datasetType, tableName);

        doExport(datasetType, ORLTableTypes.ORL_POINT_TOXICS, tableName, file);
    }

    public void testExportSucceedsUsingDefaultSettingsEvenIfFileExists() throws Exception {
        doImport("ptinv.nti99_NC.txt", DatasetTypes.ORL_POINT_TOXICS, ORLTableTypes.ORL_POINT_TOXICS);

        String datasetType = DatasetTypes.ORL_POINT_TOXICS;
        String tableName = "ptinv_nti99_NC";
        File file = createFile(datasetType, tableName);
        file.delete();
        assertTrue(file.createNewFile());

        doExport(datasetType, ORLTableTypes.ORL_POINT_TOXICS, tableName, file);
    }

    public void testExportSucceedsWhenOverwriteIsDisabledAndOutputFileDoesNotExist() throws Exception {
        String datasetType = DatasetTypes.ORL_POINT_TOXICS;
        String tableName = "ptinv_nti99_NC";
        File file = createFile(datasetType, tableName);

        doImport("ptinv.nti99_NC.txt", datasetType, ORLTableTypes.ORL_POINT_TOXICS);

        file.delete();
        doExportWithoutOverwrite(datasetType, ORLTableTypes.ORL_POINT_TOXICS, tableName, file);
    }

    public void testExportFailsWhenOverwriteIsDisabledAndOutputFileExists() throws Exception {
        String datasetType = DatasetTypes.ORL_POINT_TOXICS;
        String tableName = "ptinv_nti99_NC";
        File file = createFile(datasetType, tableName);

        doImport("ptinv.nti99_NC.txt", datasetType, ORLTableTypes.ORL_POINT_TOXICS);
        doExport(datasetType, ORLTableTypes.ORL_POINT_TOXICS, tableName, file);

        try {
            doExportWithoutOverwrite(datasetType, ORLTableTypes.ORL_POINT_TOXICS, tableName, file);
        } catch (Exception e) {
            return;
        }

        fail("should have failed to export since file has already been exported and exists");
    }

    public void testNonPoint() throws Exception {
        doImport("arinv.nonpoint.nti99_NC.txt", DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
                ORLTableTypes.ORL_AREA_NONPOINT_TOXICS);

        String datasetType = DatasetTypes.ORL_AREA_NONPOINT_TOXICS;
        String tableName = "arinv_nonpoint_nti99_NC";
        File file = createFile(datasetType, tableName);

        doExport(datasetType, ORLTableTypes.ORL_AREA_NONPOINT_TOXICS, tableName, file);
    }

    public void testOnRoadMobile() throws Exception {
        doImport("nti99.NC.onroad.SMOKE.txt", DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS,
                ORLTableTypes.ORL_ONROAD_MOBILE_TOXICS);

        String datasetType = DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS;
        String tableName = "nti99_NC_onroad_SMOKE";
        File file = createFile(datasetType, tableName);

        doExport(datasetType, ORLTableTypes.ORL_ONROAD_MOBILE_TOXICS, tableName, file);
    }

    public void testNonRoad() throws Exception {
        doImport("arinv.nonroad.nti99d_NC.new.txt", DatasetTypes.ORL_AREA_NONROAD_TOXICS,
                ORLTableTypes.ORL_AREA_NONROAD_TOXICS);

        String datasetType = DatasetTypes.ORL_AREA_NONROAD_TOXICS;
        String tableName = "arinv_nonroad_nti99d_NC_new";
        File file = createFile(datasetType, tableName);

        doExport(datasetType, ORLTableTypes.ORL_AREA_NONROAD_TOXICS, tableName, file);
    }

    private void doExportWithoutOverwrite(String datasetType, TableType tableType, String tableName, File file)
            throws Exception {
        ORLExporter exporter = ORLExporter.createWithoutOverwrite(dbSetup.getDbServer());
        EmfDataset dataset = createDataset(datasetType, tableType, tableName);

        exporter.run(dataset, file);
    }

    private void doExport(String datasetType, TableType tableType, String tableName, File file) throws Exception {
        ORLExporter exporter = ORLExporter.create(dbSetup.getDbServer());
        EmfDataset dataset = createDataset(datasetType, tableType, tableName);

        exporter.run(dataset, file);
    }

    private File createFile(String datasetType, String tableName) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String exportFileName = tempDir + "/" + datasetType + "." + tableName + ".EXPORTED_";

        return new File(exportFileName);
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

    private void doImport(final String filename, String datasetType, TableType tableType) throws Exception {
        String tableName = filename.substring(0, filename.length() - 4).replace('.', '_');

        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        // only one base type
        dataset.addTable(new Table(tableType.baseTypes()[0], tableName));
        dataset.addTable(new Table(tableType.summaryType(), tableName + "_summary"));

        Importer importer = new BaseORLImporter(dbSetup.getDbServer(), false, true);
        importer.run(new File[] { new File("test/commons/data/orl/nc", filename) }, dataset, true);
    }
}
