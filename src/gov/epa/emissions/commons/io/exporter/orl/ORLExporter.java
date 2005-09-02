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

/**
 * This exporter writes out data in the "One Pollutant Record Per Line" format.
 * It handles four dataset types: Nonpoint, Nonroad, Onroad, and Point.
 */
public class ORLExporter extends FixedFormatExporter {

    private ORLHeaderWriter headerWriter;

    private ORLBodyFactory bodyWriterFactory;

    public ORLExporter(DbServer dbServer) {
        super(dbServer);
        this.headerWriter = new ORLHeaderWriter();
        this.bodyWriterFactory = new ORLBodyFactory();
    }

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
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file.getCanonicalPath())));

            headerWriter.writeHeader(dataset, writer);

            Datasource datasource = dbServer.getEmissionsDatasource();
            // TODO: we know ORL only has a single base table, but cleaner
            // interface needed
            Table baseTable = dataset.getTables()[0];
            String qualifiedTableName = datasource.getName() + "." + baseTable.getTableName();

            Query query = datasource.query();
            ResultSet data = query.selectAll(qualifiedTableName);

            String datasetType = dataset.getDatasetType();

            ORLBody body = bodyWriterFactory.getBody(datasetType);
            body.write(data, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}
