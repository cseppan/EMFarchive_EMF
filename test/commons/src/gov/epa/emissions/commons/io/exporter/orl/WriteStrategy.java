package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;

import java.io.File;

public interface WriteStrategy {

    void write(EmfDataset dataset, File file) throws EmfException;
}