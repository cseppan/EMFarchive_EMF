package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.CommonsTestCase;
import gov.epa.emissions.commons.io.importer.TableTypes;

// FIXME: first import, before exporting.
public class ORLExporterTest extends CommonsTestCase {

    public void xtestPoint() throws Exception {
        doExport(DatasetTypes.ORL_POINT_TOXICS, TableTypes.ORL_POINT_TOXICS, "ptinv_nti99_NC");
    }
    
    public void testNonPoint() throws Exception {
        doExport(DatasetTypes.ORL_AREA_NONPOINT_TOXICS, TableTypes.ORL_AREA_NONPOINT_TOXICS, "arinv_nonpoint_nti99_NC");
    }

    public void xtestOnRoadMobile() throws Exception {
        doExport(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS, TableTypes.ORL_MOBILE_TOXICS, "nti99_NC_onroad_SMOKE");
    }

    public void xtestNonRoad() throws Exception {
        doExport(DatasetTypes.ORL_AREA_NONROAD_TOXICS, TableTypes.ORL_AREA_NONROAD_TOXICS, "nti99_NC_onroad_SMOKE");
    }

    private void doExport(String datasetType, String tableType, String tableName) throws Exception {
        Dataset dataset = createDataset(datasetType, tableType, tableName);

        ORLExporter exporter = new ORLExporter(dbSetup.getDbServer());

        String tempDir = System.getProperty("java.io.tmpdir");
        String exportFileName = tempDir + "/" + datasetType + "." + tableName + ".EXPORTED_";

        exporter.exportTableToFile(tableType, dataset, exportFileName);
    }

    private Dataset createDataset(String datasetType, String tableType, String tableName) {
        Dataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        dataset.addDataTable(tableType, tableName);
        dataset.setRegion("US");
        dataset.setCountry("US");
        dataset.setYear(1234);
        dataset.setDescription("This is the first line of an artificial description\nThis is the second line");
        
        return dataset;
    }

}
