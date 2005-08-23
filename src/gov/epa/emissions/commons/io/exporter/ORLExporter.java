package gov.epa.emissions.commons.io.exporter;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Query;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This exporter writes out data in the "One Pollutant Record Per Line" format.
 * It handles four dataset types: Nonpoint, Nonroad, Onroad, and Point.
 */
public class ORLExporter extends FixedFormatExporter {
    /* Header record command fields */
    private static final String COMMAND = "#";

    private static final String ORL_COMMAND = COMMAND + "ORL";

    private static final String TYPE_COMMAND = COMMAND + "TYPE    ";

    private static final String COUNTRY_COMMAND = COMMAND + "COUNTRY ";

    private static final String REGION_COMMAND = COMMAND + "REGION  ";

    private static final String YEAR_COMMAND = COMMAND + "YEAR    ";

    private static final String DESCRIPTION_COMMAND = COMMAND + "DESC    ";

    // A delimeter character to separate fields in the output file
    private final String DLM = ",";

    private static final Map typeHackMap;
    static {
        typeHackMap = new HashMap();
        typeHackMap.put(DatasetTypes.ORL_AREA_NONPOINT_TOXICS, "Non-point Source Inventory");
        typeHackMap.put(DatasetTypes.ORL_AREA_NONROAD_TOXICS, "Non-road Vehicle Emission Inventory");
        typeHackMap.put(DatasetTypes.ORL_MOBILE_TOXICS, "On-road Vehicle Emission Inventory");
        typeHackMap.put(DatasetTypes.ORL_POINT_TOXICS, "Point Source Inventory");
    }

    public ORLExporter(DbServer dbServer) {
        super(dbServer);
    }

    public void exportTableToFile(String tableType, Dataset dataset, String fileName) throws Exception {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

            writeHeader(dataset, writer);

            Datasource datasource = dbServer.getEmissionsDatasource();
            String tableName = (String) dataset.getDataTables().get(tableType);
            String qualifiedTableName = datasource.getName() + "." + tableName;

            // file data from table
            Query query = datasource.query();
            ResultSet data = query.selectAll(qualifiedTableName);
            ResultSetMetaData metaData = data.getMetaData();
            List columnNames = new ArrayList();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                columnNames.add(columnName);
            }

            String datasetType = dataset.getDatasetType();

            /*
             * 
             */

            // Now start writing the output data. Here are some notes
            // The database field of column 2 is the state abbreviation code.
            // Ignore it.
            // The database field called CAS is now called POLL.
            // Separate the fields by a delimeter character, usually a comma
            // Always check whether a data value is a null; if so, write out a
            // -9
            // For Annual Emisions and Average Day Emissions, use an exponential
            // format as
            // these data values can be very small
            // 
            if (datasetType.equals(DatasetTypes.ORL_AREA_NONPOINT_TOXICS)) {
                writeNonPoint(writer, data);
            }

            // For ORL Nonroad format, write out the data from the data base to
            // the export file.
            if (datasetType.equals(DatasetTypes.ORL_AREA_NONROAD_TOXICS)) {
                writeNonRoad(writer, data);
            }

            // For ORL Onroad format, write out the data from the data base to
            // the export file.
            if (datasetType.equals(DatasetTypes.ORL_MOBILE_TOXICS)) {
                writeMobile(writer, data);
            }

            // For ORL Point format, write out the data from the data base to
            // the export file.
            if (datasetType.equals(DatasetTypes.ORL_POINT_TOXICS)) {
                writePoint(writer, data);
            }

        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    private void writePoint(PrintWriter writer, ResultSet data) throws SQLException {
        int numRows;
        numRows = 0;
        while (data.next()) {
            // FIPS field
            if (data.getString(1) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.FIPS_FORMAT.format(data.getInt(1)) + DLM);

            // PLANTID field
            if (data.getString(3) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(3) + DLM);

            // POINTID field
            if (data.getString(4) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(4) + DLM);

            // STACKID field
            if (data.getString(5) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(5) + DLM);

            // SEGMENT field
            if (data.getString(6) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(6) + DLM);

            // PLANT field
            if (data.getString(7) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(7) + DLM);

            // SCC field
            if (data.getString(8) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(8) + DLM);

            // ERPTYPE field
            if (data.getString(9) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.ERPTYPE_FORMAT.format(data.getString(9)) + DLM);

            // SRCTYPE field
            if (data.getString(10) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(10) + DLM);

            // STKHGT field
            if (data.getString(11) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.STKHGT_FORMAT.format(data.getDouble(11)) + DLM);

            // STKDIAM field
            if (data.getString(12) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.STKDIAM_FORMAT.format(data.getDouble(12)) + DLM);

            // STKTEMP field
            if (data.getString(13) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.STKTEMP_FORMAT.format(data.getDouble(13)) + DLM);

            // STKFLOW field
            if (data.getString(14) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.STKFLOW_FORMAT.format(data.getDouble(14)) + DLM);

            // STKVEL field
            if (data.getString(15) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.STKVEL_FORMAT.format(data.getDouble(15)) + DLM);

            // SIC field
            if (data.getString(16) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(16) + DLM);

            // MACT field
            if (data.getString(17) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(17) + DLM);

            // NAICS field
            if (data.getString(18) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(18) + DLM);

            // CTYPE field
            if (data.getString(19) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.ERPTYPE_FORMAT.format(data.getString(19)) + DLM);

            // XLOC field
            if (data.getString(20) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.XLOC_FORMAT.format(data.getDouble(20)) + DLM);

            // YLOC field
            if (data.getString(21) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.YLOC_FORMAT.format(data.getDouble(21)) + DLM);

            // UTMZ field
            if (data.getString(22) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.UTMZ_FORMAT.format(data.getInt(22)) + DLM);

            // POLL field
            if (data.getString(23) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(23) + DLM);

            // ANN_EMIS field
            if (data.getString(24) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.ANN_EMIS_FORMAT.format(data.getDouble(24)) + DLM);

            // AVD_EMIS field
            if (data.getString(25) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble(25)) + DLM);

            // CEFF field
            if (data.getString(26) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble(26)) + DLM);

            // REFF field
            if (data.getString(27) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble(27)) + DLM);

            // CPRI field
            if (data.getString(28) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.CPRI_FORMAT.format(data.getInt(28)) + DLM);

            // CSEC field
            if (data.getString(29) == null)
                writer.print("-9");
            else
                writer.print(ORLFormats.CSEC_FORMAT.format(data.getInt(29)));

            // Close the line and count the number of rows
            writer.println();
            numRows++;
        }
    }

    private void writeMobile(PrintWriter writer, ResultSet data) throws SQLException {
        int numRows;
        numRows = 0;
        while (data.next()) {
            // FIPS field
            if (data.getString(1) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.FIPS_FORMAT.format(data.getInt(1)) + DLM);

            // SCC field
            if (data.getString(3) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(3) + DLM);

            // POLL field
            if (data.getString(4) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(4) + DLM);

            // ANN_EMIS field
            if (data.getString(5) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.ANN_EMIS_FORMAT.format(data.getDouble(5)) + DLM);

            // AVD_EMIS field
            if (data.getString(6) == null)
                writer.print("-9");
            else
                writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble(6)));

            // Close the line and count the number of rows
            writer.println();
            numRows++;
        }
    }

    private void writeNonRoad(PrintWriter writer, ResultSet data) throws SQLException {
        int numRows;
        numRows = 0;
        while (data.next()) {
            // FIPS field
            if (data.getString(1) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.FIPS_FORMAT.format(data.getInt(1)) + DLM);

            // SCC field
            if (data.getString(3) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(3) + DLM);

            // POLL field
            if (data.getString(4) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(4) + DLM);

            // ANN_EMIS field
            if (data.getString(5) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.ANN_EMIS_FORMAT.format(data.getDouble(5)) + DLM);

            // AVD_EMIS field
            if (data.getString(6) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble(6)) + DLM);

            // CEFF field
            if (data.getString(7) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble(7)) + DLM);

            // REFF field
            if (data.getString(8) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble(8)) + DLM);

            // RPEN field
            if (data.getString(9) == null)
                writer.print("-9");
            else
                writer.print(ORLFormats.RPEN_FORMAT.format(data.getDouble(9)));

            // Close the line and count the number of rows
            writer.println();
            numRows++;
        }
    }

    private void writeNonPoint(PrintWriter writer, ResultSet data) throws SQLException {
        // For ORL Nonpoint format, write out the data from the data base to the
        // export file.
        int numRows;
        numRows = 0;
        while (data.next()) {
            // FIPS field
            if (data.getString(1) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.FIPS_FORMAT.format(data.getInt(1)) + DLM);

            // SCC field
            if (data.getString(3) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(3) + DLM);

            // SIC field
            if (data.getString(4) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(4) + DLM);

            // MACT field
            if (data.getString(5) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(5) + DLM);

            // SRCTYPE field
            if (data.getString(6) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(6) + DLM);

            // NAICS field
            if (data.getString(7) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(7) + DLM);

            // POLL field
            if (data.getString(8) == null)
                writer.print("-9" + DLM);
            else
                writer.print(data.getString(8) + DLM);

            // ANN_EMIS field
            if (data.getString(9) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.ANN_EMIS_FORMAT.format(data.getDouble(9)) + DLM);

            // AVD_EMIS field
            if (data.getString(10) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble(10)) + DLM);

            // CEFF field
            if (data.getString(11) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble(11)) + DLM);

            // REFF field
            if (data.getString(12) == null)
                writer.print("-9" + DLM);
            else
                writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble(12)) + DLM);

            // RPEN field
            if (data.getString(13) == null)
                writer.print("-9");
            else
                writer.print(ORLFormats.RPEN_FORMAT.format(data.getDouble(13)));

            // Close the line and count the number of rows
            writer.println();
            numRows++;
        }
    }

    private void writeHeader(Dataset dataset, PrintWriter writer) {
        writer.println(ORL_COMMAND);

        String regionMessage = (dataset.getRegion() != null) ? dataset.getRegion() : " Region not found in database";
        writer.println(REGION_COMMAND + regionMessage);

        String countryMessage = (dataset.getCountry() != null) ? dataset.getCountry()
                : " Country not found in database";
        writer.println(COUNTRY_COMMAND + countryMessage);

        writer.println(YEAR_COMMAND
                + ((dataset.getYear() != 0) ? "" + dataset.getYear() : " Year not found in database"));

        String type = (dataset.getDatasetType() != null) ? (String) typeHackMap.get(dataset.getDatasetType())
                : " Dataset Type not found in database";
        writer.println(TYPE_COMMAND + type);

        writeDescription(dataset.getDescription(), writer);
    }

    private void writeDescription(String description, PrintWriter writer) {
        if (description == null || description.length() == 0)
            writer.println(DESCRIPTION_COMMAND + " Description not found in database");

        String[] descriptions = description.split("\\n");
        for (int i = 0; i < descriptions.length; i++) {
            writer.println(DESCRIPTION_COMMAND + descriptions[i]);
        }

    }
}
