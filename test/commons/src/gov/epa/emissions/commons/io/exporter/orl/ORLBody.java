package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ORLBody {

    private FormatterSequence formatterSequence;

    public ORLBody(FormatterSequence formatterSequence) {
        this.formatterSequence = formatterSequence;
    }

    public void write(ResultSet data, PrintWriter writer) throws SQLException {
        while (data.next()) {
            writeRecord(data, writer);
        }
    }

    private void writeRecord(ResultSet data, PrintWriter writer) throws SQLException {
        List formatters = formatterSequence.sequence();
        for (int i = 0; i < formatters.size() - 1; i++) {
            Formatter formatter = (Formatter) formatters.get(i);
            formatter.format(data, writer);

            writer.print(Formatter.DELIMITER);
        }

        // Skip the delimiter for the last one
        Formatter formatter = (Formatter) formatters.get(formatters.size() - 1);
        formatter.format(data, writer);

        writer.println();// Close the line
    }
}
