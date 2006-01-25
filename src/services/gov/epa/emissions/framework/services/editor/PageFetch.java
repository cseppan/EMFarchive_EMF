package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.services.DataAccessToken;

import java.sql.SQLException;
import java.util.Iterator;

import org.hibernate.Session;

public class PageFetch {

    private DataUpdatesCache cache;

    public PageFetch(DataUpdatesCache cache) {
        this.cache = cache;
    }

    public Page getPage(DataAccessToken token, int pageNumber, Session session) throws Exception {
        RecordsFilter filter = new RecordsFilter();

        PageReader reader = cache.reader(token);
        Page page = reader.page(pageNumber);
        ChangeSets changesets = cache.changesets(token, pageNumber, session);

        return filter.filter(page, changesets);
    }

    public int getPageCount(DataAccessToken token) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.totalPages();
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.pageByRecord(record);
    }

    public int getTotalRecords(DataAccessToken token, Session session) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.totalRecords() + netRecordCountIncreaseDueToChanges(token, session);
    }

    private int netRecordCountIncreaseDueToChanges(DataAccessToken token, Session session) throws SQLException {
        int total;
        ChangeSets changesets = cache.changesets(token, session);
        total = 0;
        for (Iterator iter = changesets.iterator(); iter.hasNext();) {
            ChangeSet element = (ChangeSet) iter.next();
            total += element.netIncrease();
        }

        return total;
    }

    public int defaultPageSize(Session session) {
        return cache.defaultPageSize(session);
    }
}
