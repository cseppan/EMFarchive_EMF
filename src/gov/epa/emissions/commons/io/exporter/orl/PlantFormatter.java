package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Plant Name
 */
public class PlantFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column that PLANT field corresponds to ?
        String value = data.getString(7);
        String evalValue = (value == null) ? "-9" : value;

        writer.print(evalValue);
    }

}
