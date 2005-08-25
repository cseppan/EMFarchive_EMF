package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Point Identification Code
 */
public class PointIdFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column that POINTID field corresponds to ?
        String value = data.getString(4);
        String evalValue = (value == null) ? "-9" : value;

        writer.print(evalValue);
    }

}
