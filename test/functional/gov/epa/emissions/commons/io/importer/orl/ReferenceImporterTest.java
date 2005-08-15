package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.io.importer.ReferenceImporter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

public class ReferenceImporterTest extends TestCase {

    private DatabaseSetup dbSetup;

    private File fieldDefsFile;

    private File referenceFilesDir;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("test/user_preferences.txt")));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        dbSetup = new DatabaseSetup(properties);
        fieldDefsFile = new File("config/field_defs.dat");
        referenceFilesDir = new File("config/refDbFiles");
    }

    public void testImportReference() throws Exception {
        System.out.println("Started Reference importer...");
        ReferenceImporter referenceImporter = new ReferenceImporter(dbSetup.getDbServer(), fieldDefsFile,
                referenceFilesDir, false);
        referenceImporter.createReferenceTables();
        System.out.println("Completed importing Reference data");
    }

}
