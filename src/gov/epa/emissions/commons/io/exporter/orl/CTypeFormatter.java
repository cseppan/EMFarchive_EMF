package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Coordinate System Type (CTYPE)
 */
public class CTypeFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column that CTYPE field corresponds to ?
        String value = data.getString(19);
        String evalValue = (value == null) ? "-9" : ErpTypeFormatter.ERPTYPE_FORMAT.format(value);
        
        writer.print(evalValue);
    }

}
