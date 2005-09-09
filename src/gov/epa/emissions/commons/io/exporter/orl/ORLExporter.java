package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.exporter.FixedFormatExporter;

import java.io.File;

/**
 * This exporter writes out data in the "One Pollutant Record Per Line" format.
 * It handles four dataset types: Nonpoint, Nonroad, Onroad, and Point. <p/>
 * 
 * By default, it overwrites an existing file.
 */
public class ORLExporter extends FixedFormatExporter {

    private WriteStrategy writeStrategy;

    private ORLExporter(DbServer dbServer, WriteStrategy strategy) {
        super(dbServer);
        this.writeStrategy = strategy;
    }

    public static ORLExporter create(DbServer dbServer) {
        return new ORLExporter(dbServer, new OverwriteStrategy(dbServer));
    }

    public static ORLExporter createWithoutOverwrite(DbServer dbServer) {
        return new ORLExporter(dbServer, new NoOverwriteStrategy(dbServer));
    }

    // FIXME: what's this gibberish ?
    // Now start writing the output data. Here are some notes
    // The database field of column 2 is the state abbreviation code.
    // Ignore it.
    // The database field called CAS is now called POLL.
    // Separate the fields by a delimeter character, usually a comma
    // Always check whether a data value is a null; if so, write out a
    // -9
    // For Annual Emisions and Average Day Emissions, use an exponential
    // format as these data values can be very small

    public void run(EmfDataset dataset, File file) throws Exception {
        writeStrategy.write(dataset, file);
    }
}
