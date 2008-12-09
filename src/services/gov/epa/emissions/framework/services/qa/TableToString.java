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
            int startingColumn=1;
            if (md.getColumnName(1).equalsIgnoreCase("record_id"))
            {
               // skip the first four columns from the dataset info and start at column 5
               startingColumn=5;
            }
            
            writeHeaderRow(md, startingColumn, columnCount);
            String row = "";
            String value = "";
                       
            while (rs.next()) {
                row = "";
                for (int i = startingColumn; i <= columnCount; i++) {
                    value = rs.getString(i);
                    if (value != null) {
                        if (value.indexOf(",") > 0 || value.indexOf(";") > 0 || value.indexOf(" ") > 0) 
                        {
                            value = "\"" + value + "\"";
                        }
                        else if (value.length()==0) value="\"\"";
                    }
                    row += (i > startingColumn ? delimiter : "") + (!rs.wasNull() ? value : "\"\"");
                }
                //the analysis engine only supports reports with more than one column, so add a dummy column value for now.
                if (columnCount == 1) row += delimiter + "\"\"";
                output.append(row + lineFeeder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException("could not convert table to string ", e);
        }
    }

    private void writeHeaderRow(ResultSetMetaData md, int startingColumn, int columnCount) throws SQLException {
        String colTypes = "#COLUMN_TYPES=";
        String colNames = ""; 
        
        for (int i = startingColumn; i <= columnCount; i++) {
            colTypes += md.getColumnTypeName(i) + "(" + md.getPrecision(i) + ")" + (i < columnCount ? "|" : "");
            colNames += (i > startingColumn ? delimiter : "") + md.getColumnName(i).toLowerCase();
        }
        //the analysis engine only supports reports with more than one column, so add a dummy column for now.
        if (columnCount == 1) {
            colTypes += "|varchar(1)";
            colNames += delimiter + "extra";
        }
        output.append(colTypes + lineFeeder + colNames + lineFeeder);
    }
}
