package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Secondary Control Equipment Code (CSEC)
 */
public class CsecFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column that CSEC field corresponds to ?
        if (data.getString(29) == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.CSEC_FORMAT.format(data.getInt(29)));
    }

}
