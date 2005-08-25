package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Plant Identification Code
 */
public class PlantIdFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column name for PLANTID field
        String value = data.getString(3);
        String evalValue = (value == null) ? "-9" : value;

        writer.print(evalValue);
    }

}
