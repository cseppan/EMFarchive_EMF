package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Stack Gas Flow Rate (STKFLOW)
 */
public class StkFlowFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("stkflow") == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.STKFLOW_FORMAT.format(data.getDouble("stkflow")));
    }

}
