package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.importer.ReferenceImporter;

public class ReferenceImporterTest extends EmissionsDataSetupTestCase {

    public void testImportReference() throws Exception {
        ReferenceImporter referenceImporter = new ReferenceImporter(dbSetup.getDbServer(), fieldDefsFile,
                referenceFilesDir, false);
        referenceImporter.createReferenceTables();
    }

}
