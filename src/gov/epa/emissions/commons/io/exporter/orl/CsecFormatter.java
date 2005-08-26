package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Secondary Control Equipment Code (CSEC)
 */
public class CsecFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("csec") == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.CSEC_FORMAT.format(data.getInt("csec")));
    }

}
