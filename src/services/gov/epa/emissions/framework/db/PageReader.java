package gov.epa.emissions.framework.db;

import gov.epa.emissions.framework.services.Page;

import java.sql.SQLException;

public class PageReader {

    private int pageSize;

    private ScrollableRecords records;

    public PageReader(int pageSize, ScrollableRecords records) {
        this.pageSize = pageSize;
        this.records = records;
    }

    public int count() throws SQLException {
        return records.rowCount() / pageSize;
    }

    public Page page(int position) throws SQLException {
        return position <= count() ? new Page() : null;
    }

}
