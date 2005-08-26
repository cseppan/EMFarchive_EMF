package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Coordinate System Type (CTYPE)
 */
public class CTypeFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        String value = data.getString("ctype");
        String evalValue = (value == null) ? "-9" : ErpTypeFormatter.ERPTYPE_FORMAT.format(value);
        
        writer.print(evalValue);
    }

}
