package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.io.importer.ReferenceTables;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

public class ReferenceTablesTest extends TestCase {

    private DatabaseSetup dbSetup;

    private File referenceFilesDir;

    private void initUsingPostgres() throws Exception {
        init(new File("test/commons/postgres.conf"));
    }

    private void initUsingMysql() throws Exception {
        init(new File("test/commons/mysql.conf"));
    }

    private void init(File conf) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        dbSetup = new DatabaseSetup(properties);

        referenceFilesDir = new File("config/refDbFiles");
    }

    public void testCreateAddtionalTablesUsingPostgres() throws Exception {
        initUsingPostgres();
        doTestCreateAdditionalTables();
    }

    public void testCreateAddtionalTablesUsingMysql() throws Exception {
        initUsingMysql();
        doTestCreateAdditionalTables();
    }

    private void doTestCreateAdditionalTables() throws Exception {
        ReferenceTables tables = new ReferenceTables(referenceFilesDir, dbSetup.getDbServer().getTypeMapper());
        tables.createAdditionRefTables(dbSetup.getDbServer().getReferenceDatasource());
    }

}
