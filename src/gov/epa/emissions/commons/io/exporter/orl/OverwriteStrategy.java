package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.framework.EmfException;

import java.io.File;

public interface OverwriteStrategy {

    void verifyWritable(File file) throws EmfException;
}