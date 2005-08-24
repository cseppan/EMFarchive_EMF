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

    private void doTestNonPoint(String fileSuffix) throws Exception {
        String datasetType = DatasetTypes.ORL_AREA_NONPOINT_TOXICS;
        String tableType = TableTypes.ORL_AREA_NONPOINT_TOXICS;

        String importFilenamePrefix = "arinv.nonpoint.nti99_NC";
        String tableName = importFilenamePrefix.replace('.', '_');

        Dataset dataset = createDataset(datasetType, tableType, tableName);

        ORLExporter exporter = new ORLExporter(dbSetup.getDbServer());

        String tempDir = System.getProperty("java.io.tmpdir");
        String exportFileName = tempDir + "/" + importFilenamePrefix + ".EXPORTED_" + fileSuffix;

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
