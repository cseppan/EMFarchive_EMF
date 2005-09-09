package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;

import java.io.File;

/**
 * overwrites file, if exists
 */
public class OverwriteStrategy implements WriteStrategy {

    private ORLWriter writer;

    public OverwriteStrategy(DbServer dbServer) {
        writer = new ORLWriter(dbServer);
    }

    public void write(EmfDataset dataset, File file) throws EmfException {
        writer.write(dataset, file);
    }

}