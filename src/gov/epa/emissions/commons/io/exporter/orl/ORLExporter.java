package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Query;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.exporter.FixedFormatExporter;
import gov.epa.emissions.commons.io.importer.DatasetTypes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;

/**
 * This exporter writes out data in the "One Pollutant Record Per Line" format.
 * It handles four dataset types: Nonpoint, Nonroad, Onroad, and Point.
 */
public class ORLExporter extends FixedFormatExporter {

    private ORLHeaderWriter headerWriter;

    public ORLExporter(DbServer dbServer) {
        super(dbServer);
        this.headerWriter = new ORLHeaderWriter();
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
    public void exportTableToFile(String tableType, Dataset dataset, String fileName) throws Exception {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

            headerWriter.writeHeader(dataset, writer);

            Datasource datasource = dbServer.getEmissionsDatasource();
            String tableName = (String) dataset.getDataTables().get(tableType);
            String qualifiedTableName = datasource.getName() + "." + tableName;

            Query query = datasource.query();
            ResultSet data = query.selectAll(qualifiedTableName);

            String datasetType = dataset.getDatasetType();

            ORLBody body = getBody(datasetType);
            body.write(writer, data);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    // TODO: convert it to factory
    private ORLBody getBody(String datasetType) {
        if (datasetType.equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS))
            return new NonPointBody();

        if (datasetType.equals(DatasetTypes.ORL_AREA_NONROAD_TOXICS))
            return new NonRoadBody();

        if (datasetType.equals(DatasetTypes.ORL_MOBILE_TOXICS))
            return new MobileBody();

        if (datasetType.equals(DatasetTypes.ORL_POINT_TOXICS))
            return new PointBody();

        return null;
    }
}
