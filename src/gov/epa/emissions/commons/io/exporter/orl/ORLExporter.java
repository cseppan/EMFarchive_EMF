package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Query;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.exporter.FixedFormatExporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This exporter writes out data in the "One Pollutant Record Per Line" format.
 * It handles four dataset types: Nonpoint, Nonroad, Onroad, and Point. <p/>
 * 
 * By default, it overwrites an existing file.
 */
public class ORLExporter extends FixedFormatExporter {

    private ORLHeaderWriter headerWriter;

    private ORLBodyFactory bodyFactory;

    private OverwriteStrategy overwriteStrategy;

    private ORLExporter(DbServer dbServer, OverwriteStrategy strategy) {
        super(dbServer);
        this.overwriteStrategy = strategy;
        this.headerWriter = new ORLHeaderWriter();
        this.bodyFactory = new ORLBodyFactory();
    }

    static public ORLExporter create(DbServer dbServer) {
        return new ORLExporter(dbServer, new ForceOverwriteStrategy());
    }

    public static ORLExporter createWithoutOverwrite(DbServer dbServer) {
        return new ORLExporter(dbServer, new NoOverwriteStrategy());
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
        overwriteStrategy.verifyWritable(file);

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file.getCanonicalPath())));
            writeHeader(dataset, writer);
            writeBody(dataset, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeBody(EmfDataset dataset, PrintWriter writer) throws SQLException {
        Datasource datasource = dbServer.getEmissionsDatasource();

        // TODO: we know ORL only has a single base table, but cleaner
        // interface needed
        Table baseTable = dataset.getTables()[0];
        String qualifiedTableName = datasource.getName() + "." + baseTable.getTableName();

        Query query = datasource.query();
        ResultSet data = query.selectAll(qualifiedTableName);

        String datasetType = dataset.getDatasetType();

        ORLBody body = bodyFactory.getBody(datasetType);
        body.write(data, writer);
    }

    private void writeHeader(EmfDataset dataset, PrintWriter writer) {
        headerWriter.writeHeader(dataset, writer);
    }
}
