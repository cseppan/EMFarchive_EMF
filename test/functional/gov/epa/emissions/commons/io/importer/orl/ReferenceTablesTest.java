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

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("test/user_preferences.txt")));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        dbSetup = new DatabaseSetup(properties);

        referenceFilesDir = new File("config/refDbFiles");
    }

    public void testCreateAddtionalTables() throws Exception {
        System.out.println("Started adding reference tables...");

        ReferenceTables tables = new ReferenceTables(referenceFilesDir, dbSetup.getDbServer().getTypeMapper());
        tables.createAdditionRefTables(dbSetup.getDbServer().getReferenceDatasource());
        
        System.out.println("Completed adding reference tables.");
    }

}
