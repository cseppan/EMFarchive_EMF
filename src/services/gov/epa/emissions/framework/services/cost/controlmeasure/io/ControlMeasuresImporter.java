package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;

public class ControlMeasuresImporter implements Importer {

    private CMImporters cmImporters;

    public ControlMeasuresImporter(File folder, String[] fileNames, HibernateSessionFactory factory)
            throws EmfException, ImporterException {
        File[] files = fileNames(folder, fileNames);
        ControlMeasuresImportIdentifier types = new ControlMeasuresImportIdentifier(files, factory);
        cmImporters = types.cmImporters();
    }

    private File[] fileNames(File folder, String[] fileNames) throws ImporterException {
        int length = fileNames.length;
        if (length < 3 || length > 4) {
            throw new ImporterException("Select between 3 to 4 files");
        }
        File[] files = new File[length];
        for (int i = 0; i < length; i++)
            files[i] = new File(folder, fileNames[i]);

        FileVerifier verifier = new FileVerifier();
        for (int i = 0; i < length; i++)
            verifier.shouldExist(files[i]);

        return files;
    }

    public void run() throws ImporterException {
        runSummary();

    }

    private void runSummary() throws ImporterException {
        CMSummaryImporter summary = cmImporters.summaryImporter();
        summary.run();
    }

}
