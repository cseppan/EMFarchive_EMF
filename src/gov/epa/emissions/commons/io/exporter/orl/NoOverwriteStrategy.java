package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.framework.EmfException;

import java.io.File;

public class NoOverwriteStrategy implements OverwriteStrategy {

    public void verifyWritable(File file) throws EmfException {
        if (file.exists())
            throw new EmfException("Cannot export as file - " + file.getAbsolutePath() + " exists");
    }

}
