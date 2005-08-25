package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class YLocFormatter implements Formatter {

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column that YLOC field corresponds to ?
        if (data.getString(21) == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.YLOC_FORMAT.format(data.getDouble(21)));
    }

}
