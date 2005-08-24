package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ORLBody {
    void write(PrintWriter writer, ResultSet data) throws SQLException;
}
