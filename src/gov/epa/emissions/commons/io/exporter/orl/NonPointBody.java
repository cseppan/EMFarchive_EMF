package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class NonPointBody implements ORLBody {

    public void write(PrintWriter writer, ResultSet data) throws SQLException {
        ResultSetMetaData meta = data.getMetaData();

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            System.err.print(meta.getColumnName(i) + "\t");
        }
        System.err.println("Total Columns - " + meta.getColumnCount());
        while (data.next()) {
            writeRecord(writer, data);
        }
    }

    private void writeRecord(PrintWriter writer, ResultSet data) throws SQLException {
        // FIPS field
        new FipsFormatter().format(data, writer);

        // SCC field
        new SccFormatter().format(data, writer);

        // SIC field
        new SicFormatter().format(data, writer);

        // MACT field
        if (data.getString("MACT") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(data.getString("MACT") + Formatter.DELIMITER);

        // SRCTYPE field
        if (data.getString("SRCTYPE") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(data.getString("SRCTYPE") + Formatter.DELIMITER);

        // NAICS field
        if (data.getString("NAICS") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(data.getString("NAICS") + Formatter.DELIMITER);

        // POLL field
        if (data.getString(8) == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(data.getString(8) + Formatter.DELIMITER);

        // ANN_EMIS field
        if (data.getString("ANN_EMIS") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(ORLFormats.ANN_EMIS_FORMAT.format(data.getDouble("ANN_EMIS")) + Formatter.DELIMITER);

        // AVD_EMIS field
        if (data.getString("AVD_EMIS") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble("AVD_EMIS")) + Formatter.DELIMITER);

        // CEFF field
        if (data.getString("CEFF") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble("CEFF")) + Formatter.DELIMITER);

        // REFF field
        if (data.getString("REFF") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble("REFF")) + Formatter.DELIMITER);

        // RPEN field
        String value = data.getString("RPEN");
        if (value == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.RPEN_FORMAT.format(value));

        // Close the line
        writer.println();
    }
}
