package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.io.importer.ReferenceImporter;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ReferenceImporterRunner {

    public static void main(String[] args) throws Exception {
        // common setup for importers
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("test/user_preferences.txt")));
        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");
        
        DatabaseSetup dbSetup = new DatabaseSetup(properties);
        dbSetup.init();
        // end setup

        DefaultUserInteractor.set(new GUIUserInteractor());

        File fieldDefsFile = new File("config/field_defs.dat");
        File referenceFilesDir = new File("config/refDbFiles");
        
        System.out.println("Started Reference importer...");
        new ReferenceImporter(dbSetup.getDbServer(), fieldDefsFile, referenceFilesDir, false).createReferenceTables();
        System.out.println("Completed importing Reference data");
    }
}
