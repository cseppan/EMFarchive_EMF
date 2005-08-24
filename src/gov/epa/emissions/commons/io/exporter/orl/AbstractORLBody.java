package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractORLBody implements ORLBody {

    abstract protected List getFormatters();

    public void write(ResultSet data, PrintWriter writer) throws SQLException {
        while (data.next()) {
            writeRecord(data, writer);
        }
    }

    private void writeRecord(ResultSet data, PrintWriter writer) throws SQLException {
        List formatters = getFormatters();

        for (int i = 0; i < formatters.size() - 1; i++) {
            Formatter element = (Formatter) formatters.get(i);
            element.format(data, writer);

            writer.print(Formatter.DELIMITER);
        }

        // Skip the delimite for the last one
        Formatter element = (Formatter) formatters.get(formatters.size() - 1);
        element.format(data, writer);

        writer.println();// Close the line
    }
}
