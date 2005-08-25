package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

/**
 * Average-day Emissions (AVD_EMIS)
 */
public class AvdEmisFormatter implements Formatter {

    public static final Format FORMAT = new Format("%14.7e");

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("AVD_EMIS") == null)
            writer.print("-9");
        else
            writer.print(FORMAT.format(data.getDouble("AVD_EMIS")));
    }

}
