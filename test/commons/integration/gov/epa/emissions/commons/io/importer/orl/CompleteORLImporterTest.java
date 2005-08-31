package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.importer.Importer;

import java.io.File;

public class CompleteORLImporterTest extends ORLImporterTestCase {

    protected void doImport(String filename, Dataset dataset) throws Exception {
        Importer importer = new CompleteORLImporter(dbSetup.getDbServer(), false, true);
        importer.run(new File[] { new File("test/commons/data/orl/nc", filename) }, dataset, true);
    }

}
