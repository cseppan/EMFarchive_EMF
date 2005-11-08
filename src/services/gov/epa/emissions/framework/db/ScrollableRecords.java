package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.Datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ScrollableRecords {

    private Datasource datasource;

    private String query;

    private ResultSet resultSet;

    private ResultSetMetaData metadata;

    public ScrollableRecords(Datasource datasource, String query) {
        this.datasource = datasource;
        this.query = query;
    }

    public void execute() throws SQLException {
        Connection connection = datasource.getConnection();
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

        resultSet = stmt.executeQuery(query);
        metadata = resultSet.getMetaData();
    }

    public int rowCount() throws SQLException {
        int current = position();

        resultSet.last();
        try {
            return position();
        } finally {
            resultSet.absolute(current);
        }
    }

    public int position() throws SQLException {
        return resultSet.getRow();
    }

    public void forward(int count) throws SQLException {
        resultSet.relative(count);
    }

    public void backward(int count) throws SQLException {
        resultSet.relative(-count);
    }

    public void moveTo(int index) throws SQLException {
        resultSet.absolute(index);

    }

    public boolean available() throws SQLException {
        // TODO: is this a serious hit to the ResultSet's cursor ?
        return position() < rowCount();
    }

    public Record next() throws SQLException {
        if (!resultSet.next())
            return null;// TODO: is NullRecord better?

        Record record = new Record();
        for (int i = 1; i <= columnCount(); i++)
            record.add(resultSet.getString(i));

        return record;// TODO: load data
    }

    private int columnCount() throws SQLException {
        return metadata.getColumnCount();
    }

    public void close() throws SQLException {
        resultSet.close();
    }

    /**
     * @return returns a range of records inclusive of start and end
     * @throws SQLException
     */
    public Record[] range(int start, int end) throws SQLException {
        List range = new ArrayList();
        moveTo(start);
        for (int i = start; i <= end; i++)
            range.add(next());

        return (Record[]) range.toArray(new Record[0]);
    }

}
