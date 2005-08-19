package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.TableTypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

public class ORLImporterTest extends TestCase {

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
        doTestNonPoint();
    }

    private void doTestNonPoint() throws Exception {
        run("arinv.nonpoint.nti99_NC.txt", DatasetTypes.ORL_AREA_NONPOINT_TOXICS, TableTypes.ORL_AREA_NONPOINT_TOXICS);
    }

    public void testNonPointUsingMysql() throws Exception {
        useMysql();
        doTestNonPoint();
    }

    // FIXME: change the columns from numbers to prefix alphabets
    public void TODO_testNonRoad() throws Exception {
        run("arinv.nonroad.nti99d_NC.new.txt", DatasetTypes.ORL_AREA_NONROAD_TOXICS, TableTypes.ORL_AREA_NONROAD_TOXICS);
    }

    public void TODO_testPoint() {
        // String datasetType = DatasetTypes.ORL_POINT_TOXICS;
        // String tableType = TableTypes.ORL_POINT_TOXICS;
        // String dbFile = "ptinv.nti99_NC.100.txt";
    }

    public void TODO_testMobile() {
        // String datasetType = DatasetTypes.ORL_MOBILE_TOXICS;
        // String tableType = TableTypes.ORL_MOBILE_TOXICS;
        // String dbFile = "nti99.NC.onroad.SMOKE.100.txt";
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
