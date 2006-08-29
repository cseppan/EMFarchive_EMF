package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ControlMeasuresImporter implements Importer {

    private CMImporters cmImporters;

    private Map controlMeasures;

    public ControlMeasuresImporter(File folder, String[] fileNames, HibernateSessionFactory factory)
            throws EmfException, ImporterException {
        File[] files = fileNames(folder, fileNames);
        ControlMeasuresImportIdentifier types = new ControlMeasuresImportIdentifier(files, factory);
        cmImporters = types.cmImporters();
        controlMeasures = new HashMap();
    }

    public void run() throws ImporterException {
        runSummary(controlMeasures);
        runEfficiencyRecords(controlMeasures);
        runSCC(controlMeasures);
        //TODO: read reference file
    }

    public ControlMeasure[] controlMeasures() {
        Iterator keys = controlMeasures.keySet().iterator();
        ControlMeasure[] measures = new ControlMeasure[controlMeasures.size()];
        int count = 0;
        while (keys.hasNext()) {
            measures[count++] = (ControlMeasure) controlMeasures.get(keys.next());
        }
        return measures;
    }

    private void runSummary(Map controlMeasures) throws ImporterException {
        CMSummaryImporter summary = cmImporters.summaryImporter();
        summary.run(controlMeasures);
    }

    private void runEfficiencyRecords(Map controlMeasures) throws ImporterException {
        CMEfficiencyImporter efficiency = cmImporters.efficiencyImporter();
        efficiency.run(controlMeasures);

    }

    private void runSCC(Map controlMeasures) throws ImporterException {
        CMSCCImporter sccImporter = cmImporters.sccImporter();
        sccImporter.run(controlMeasures);
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

}
