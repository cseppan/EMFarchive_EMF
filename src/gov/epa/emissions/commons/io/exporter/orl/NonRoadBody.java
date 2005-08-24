package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NonRoadBody implements ORLBody {

    public void write(ResultSet data, PrintWriter writer) throws SQLException {
        while (data.next()) {
            // FIPS field
            if (data.getString(1) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(FipsFormatter.FORMAT.format(data.getInt(1)) + Formatter.DELIMITER);

            // SCC field
            if (data.getString(3) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(3) + Formatter.DELIMITER);

            // POLL field
            if (data.getString(4) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(4) + Formatter.DELIMITER);

            // ANN_EMIS field
            if (data.getString(5) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(AnnEmisFormatter.FORMAT.format(data.getDouble(5)) + Formatter.DELIMITER);

            // AVD_EMIS field
            if (data.getString(6) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(AvdEmisFormatter.FORMAT.format(data.getDouble(6)) + Formatter.DELIMITER);

            // CEFF field
            if (data.getString(7) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(CeffFormatter.FORMAT.format(data.getDouble(7)) + Formatter.DELIMITER);

            // REFF field
            if (data.getString(8) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ReffFormatter.FORMAT.format(data.getDouble(8)) + Formatter.DELIMITER);

            // RPEN field
            if (data.getString(9) == null)
                writer.print("-9");
            else
                writer.print(RpenFormatter.FORMAT.format(data.getDouble(9)));

            // Close the line and count the number of rows
            writer.println();
        }
    }

}
