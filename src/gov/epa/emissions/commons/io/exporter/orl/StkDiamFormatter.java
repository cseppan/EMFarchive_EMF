package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Stack Diameter (STKDIAM)
 */
public class StkDiamFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("stkdiam") == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.STKDIAM_FORMAT.format(data.getDouble("stkdiam")));

    }

}
