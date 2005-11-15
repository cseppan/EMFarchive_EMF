package gov.epa.emissions.framework.db;

import gov.epa.emissions.framework.services.DbRecord;
import gov.epa.emissions.framework.services.Page;

import java.sql.SQLException;

public class PageReader {
    private int pageSize;

    private ScrollableRecords scrollableRecords;

    public PageReader(int pageSize, ScrollableRecords scrollableRecords) {
        this.pageSize = pageSize;
        this.scrollableRecords = scrollableRecords;
    }

    public void init() throws SQLException {
        scrollableRecords.execute();
    }

    public int pageCount() throws SQLException {
        float val = (float) scrollableRecords.total() / pageSize;
        return (int) Math.ceil(val);
    }

    public Page page(int pageNumber) throws SQLException {
        int actualPage = pageNumber - 1; // page '1' maps to '0'
        if (actualPage > pageCount())
            return null;

        int start = actualPage * pageSize;
        int end = start + pageSize - 1;// since, end is inclusive in the range
        DbRecord[] records = scrollableRecords.range(start, end);

        Page page = new Page();
        page.setRecords(records);

        return page;
    }

    public int recordsCount() throws SQLException {
        return scrollableRecords.total();
    }

}
