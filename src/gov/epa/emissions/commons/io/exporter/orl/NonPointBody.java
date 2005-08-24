package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NonPointBody implements ORLBody {

    public void write(PrintWriter writer, ResultSet data) throws SQLException {
        while (data.next()) {
            writeRecord(writer, data);
        }
    }

    private void writeRecord(PrintWriter writer, ResultSet data) throws SQLException {
        // FIPS field
        if (data.getString(1) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(ORLFormats.FIPS_FORMAT.format(data.getInt(1)) + DELIMITER);

        // SCC field
        if (data.getString(3) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(data.getString(3) + DELIMITER);

        // SIC field
        if (data.getString(4) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(data.getString(4) + DELIMITER);

        // MACT field
        if (data.getString(5) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(data.getString(5) + DELIMITER);

        // SRCTYPE field
        if (data.getString(6) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(data.getString(6) + DELIMITER);

        // NAICS field
        if (data.getString(7) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(data.getString(7) + DELIMITER);

        // POLL field
        if (data.getString(8) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(data.getString(8) + DELIMITER);

        // ANN_EMIS field
        if (data.getString(9) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(ORLFormats.ANN_EMIS_FORMAT.format(data.getDouble(9)) + DELIMITER);

        // AVD_EMIS field
        if (data.getString(10) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble(10)) + DELIMITER);

        // CEFF field
        if (data.getString(11) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble(11)) + DELIMITER);

        // REFF field
        if (data.getString(12) == null)
            writer.print("-9" + DELIMITER);
        else
            writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble(12)) + DELIMITER);

        // RPEN field
        if (data.getString(13) == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.RPEN_FORMAT.format(data.getDouble(13)));

        // Close the line and count the number of rows
        writer.println();
    }

}
