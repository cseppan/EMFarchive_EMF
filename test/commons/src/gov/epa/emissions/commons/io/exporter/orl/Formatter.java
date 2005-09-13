package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

interface Formatter {
    static final String DELIMITER = ",";

    void format(ResultSet data, PrintWriter writer) throws SQLException;
}