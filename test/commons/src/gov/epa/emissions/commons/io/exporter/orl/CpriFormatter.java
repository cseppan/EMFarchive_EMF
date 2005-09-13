package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Primary Control Equipment Code (CPRI)
 */
public class CpriFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("cpri") == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.CPRI_FORMAT.format(data.getInt("cpri")));
    }

}
