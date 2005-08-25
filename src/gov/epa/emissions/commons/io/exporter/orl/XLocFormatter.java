package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class XLocFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column that XLOC field corresponds to ?
        if (data.getString(20) == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.XLOC_FORMAT.format(data.getDouble(20)));
    }

}
