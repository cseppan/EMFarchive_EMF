package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NonRoadBody implements ORLBody {

    public void write(PrintWriter writer, ResultSet data) throws SQLException {
        while (data.next()) {
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

            // POLL field
            if (data.getString(4) == null)
                writer.print("-9" + DELIMITER);
            else
                writer.print(data.getString(4) + DELIMITER);

            // ANN_EMIS field
            if (data.getString(5) == null)
                writer.print("-9" + DELIMITER);
            else
                writer.print(ORLFormats.ANN_EMIS_FORMAT.format(data.getDouble(5)) + DELIMITER);

            // AVD_EMIS field
            if (data.getString(6) == null)
                writer.print("-9" + DELIMITER);
            else
                writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble(6)) + DELIMITER);

            // CEFF field
            if (data.getString(7) == null)
                writer.print("-9" + DELIMITER);
            else
                writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble(7)) + DELIMITER);

            // REFF field
            if (data.getString(8) == null)
                writer.print("-9" + DELIMITER);
            else
                writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble(8)) + DELIMITER);

            // RPEN field
            if (data.getString(9) == null)
                writer.print("-9");
            else
                writer.print(ORLFormats.RPEN_FORMAT.format(data.getDouble(9)));

            // Close the line and count the number of rows
            writer.println();
        }
    }

}
