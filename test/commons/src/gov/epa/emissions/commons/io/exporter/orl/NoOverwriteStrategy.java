package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;

import java.io.File;

public class NoOverwriteStrategy implements WriteStrategy {

    private ORLWriter writer;

    public NoOverwriteStrategy(DbServer dbServer) {
        writer = new ORLWriter(dbServer);
    }

    public void write(EmfDataset dataset, File file) throws EmfException {
        if (file.exists())
            throw new EmfException("Cannot export as file - " + file.getAbsolutePath() + " exists");

        writer.write(dataset, file);
    }

}
