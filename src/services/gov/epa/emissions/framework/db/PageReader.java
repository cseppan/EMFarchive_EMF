package gov.epa.emissions.framework.db;

import gov.epa.emissions.framework.services.DbRecord;
import gov.epa.emissions.framework.services.Page;

import java.sql.SQLException;

public class PageReader {
    private int pageSize;

    private ScrollableRecords scrollableRecords;

    public PageReader(int pageSize, ScrollableRecords scrollableRecords) throws SQLException {
        this.pageSize = pageSize;
        this.scrollableRecords = scrollableRecords;
        scrollableRecords.execute();
    }

    public int totalRecords() throws SQLException {
        return scrollableRecords.total();
    }

    public int totalPages() throws SQLException {
        return identifyPage(scrollableRecords.total());
    }

    /**
     * @param record
     *            starts at index '1' through n (total records)
     */
    public Page pageByRecord(int record) throws SQLException {
        return page(identifyPage(record));
    }

    private int identifyPage(int record) {
        float val = (float) record / pageSize;
        return (int) Math.ceil(val);
    }

    /**
     * 
     * @param pageNumber
     *            starts at index '1' through n (total pages)
     */
    public Page page(int pageNumber) throws SQLException {
        int actualPage = pageNumber - 1; // page '1' maps to page '0'
        if (actualPage > totalPages())
            return null;

        int start = actualPage * pageSize;
        int end = start + pageSize - 1;// since, end is inclusive in the range
        DbRecord[] records = scrollableRecords.range(start, end);

        Page page = new Page();
        page.setRecords(records);

        return page;
    }

}
