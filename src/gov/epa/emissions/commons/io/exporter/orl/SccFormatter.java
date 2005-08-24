package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SccFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        String value = data.getString("SCC");
        if (value == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(value + DELIMITER);
    }

}
