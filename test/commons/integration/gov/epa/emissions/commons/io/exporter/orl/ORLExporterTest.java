package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.exporter.orl.ORLExporter;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.TableTypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

// TODO: the table is the same for Non Point, Non Road and On Road until Importer can import all those types. 
// Currently, it only imports the Non Point
public class ORLExporterTest extends TestCase {

    private DatabaseSetup dbSetup;

    private void usePostgres() throws Exception {
        init(new File("test/commons/postgres.conf"));
    }

    private void useMysql() throws Exception {
        init(new File("test/commons/mysql.conf"));
    }

    private void init(File conf) throws IOException, FileNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        dbSetup = new DatabaseSetup(properties);
    }

    public void testNonPointUsingPostgres() throws Exception {
        usePostgres();
        doTestNonPoint("POSTGRES");
    }

    public void testNonPointUsingMysql() throws Exception {
        useMysql();
        doTestNonPoint("MYSQL");
    }

    public void testOnRoadUsingPostgres() throws Exception {
        usePostgres();
        doTestOnRoad("POSTGRES");
    }

    public void testOnRoadUsingMysql() throws Exception {
        useMysql();
        doTestOnRoad("MySQL");
    }

    public void testNonRoadUsingPostgres() throws Exception {
        usePostgres();
        doTestNonRoad("POSTGRES");
    }

    public void testNonRoadUsingMysql() throws Exception {
        useMysql();
        doTestNonRoad("POSTGRES");
    }

    private void doTestNonPoint(String fileSuffix) throws Exception {
        doExport(fileSuffix, DatasetTypes.ORL_AREA_NONPOINT_TOXICS, TableTypes.ORL_AREA_NONPOINT_TOXICS,
                "arinv_nonpoint_nti99_NC");
    }

    private void doTestOnRoad(String fileSuffix) throws Exception {
        doExport(fileSuffix, DatasetTypes.ORL_ON_ROAD_TOXICS, TableTypes.ORL_MOBILE_TOXICS, "arinv_nonpoint_nti99_NC");
    }

    private void doTestNonRoad(String fileSuffix) throws Exception {
        doExport(fileSuffix, DatasetTypes.ORL_AREA_NONROAD_TOXICS, TableTypes.ORL_AREA_NONROAD_TOXICS,
                "arinv_nonpoint_nti99_NC");
    }

    private void doExport(String fileSuffix, String datasetType, String tableType, String tableName) throws Exception {
        Dataset dataset = createDataset(datasetType, tableType, tableName);

        ORLExporter exporter = new ORLExporter(dbSetup.getDbServer());

        String tempDir = System.getProperty("java.io.tmpdir");
        String exportFileName = tempDir + "/" + datasetType + "." + tableName + ".EXPORTED_" + fileSuffix;

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
