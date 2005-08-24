package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CasFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        String value = data.getString("CAS");
        String evalValue = (value == null) ? "-9" : value;

        writer.print(evalValue + DELIMITER);
    }

}
