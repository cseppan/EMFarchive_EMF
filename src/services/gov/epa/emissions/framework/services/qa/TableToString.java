package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.ExporterException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class TableToString {

    private String qualifiedTableName;

    private Datasource datasource;

    private String delimiter;

    private StringBuffer output;

    protected String lineFeeder = System.getProperty("line.separator");

    public TableToString(DbServer dbServer, String qualifiedTableName, String delimiter) {
        this.qualifiedTableName = qualifiedTableName;
        this.datasource = dbServer.getEmissionsDatasource();
        this.delimiter = delimiter;
        this.output = new StringBuffer();
    }

    public String toString() {
        try {
            writeToString();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return output.toString();
    }

    private void writeToString() throws Exception {
        try {
            ResultSet rs = datasource.query().executeQuery("select * from " + qualifiedTableName);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            writeHeaderRow(md, columnCount);
            String row = "";
            String value = "";
            while (rs.next()) {
                row = "";
                for (int i = 1; i <= columnCount; i++) {
                    value = rs.getString(i);
                    if (value != null) {
                        if (value.indexOf(",") > 0 || value.indexOf(";") > 0 || value.indexOf(" ") > 0) 
                        {
                            value = "\"" + value + "\"";
                        }
                        else if (value.length()==0) value="\"\"";
                    }
                    row += (i > 1 ? delimiter : "") + (!rs.wasNull() ? value : "\"\"");
                }
                output.append(row + lineFeeder);
            }
        } catch (SQLException e) {
            throw new ExporterException("could not export lines ", e);
        }
    }

    private void writeHeaderRow(ResultSetMetaData md, int columnCount) throws SQLException {
        String colTypes = "#COLUMN_TYPES=";
        String colNames = ""; 
        
        for (int i = 1; i <= columnCount; i++) {
            colTypes += md.getColumnTypeName(i) + "(" + md.getPrecision(i) + ")" + (i < columnCount ? "|" : "");
            colNames += (i > 1 ? delimiter : "") + md.getColumnName(i).toLowerCase();
        }
        
        output.append(colTypes + lineFeeder + colNames + lineFeeder);
    }
}
