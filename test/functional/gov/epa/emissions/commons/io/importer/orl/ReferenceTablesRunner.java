package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.io.importer.ReferenceTables;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ReferenceTablesRunner {

    public static void main(String[] args) throws Exception {
        // common setup for importers
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("test/user_preferences.txt")));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        DatabaseSetup dbSetup = new DatabaseSetup(properties);
        // end setup

        File referenceFilesDir = new File("config/refDbFiles");
        ReferenceTables tables = new ReferenceTables(referenceFilesDir, dbSetup.getDbServer().getTypeMapper());

        System.out.println("Started adding reference tables...");
        tables.createAdditionRefTables(dbSetup.getDbServer().getReferenceDatasource());
        System.out.println("Completed adding reference tables.");
    }
}
