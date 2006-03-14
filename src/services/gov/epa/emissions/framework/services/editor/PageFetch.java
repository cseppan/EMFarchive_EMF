package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.services.editor.ChangeSets.ChangeSetsIterator;

import org.hibernate.Session;

public class PageFetch {

    private DataAccessCache cache;

    private RecordsFilter filter;

    public PageFetch(DataAccessCache cache) {
        this.cache = cache;
        filter = new RecordsFilter();
    }

    public Page getPage(DataAccessToken token, int pageNumber, Session session) throws Exception {
        Page page = filteredPage(token, pageNumber, session);
        setRange(page, token, session);

        return page;
    }

    void setRange(Page page, DataAccessToken token, Session session) throws Exception {
        int previousPage = page.getNumber() - 1;
        int previousPagesTotal = totalSizeOfPreviousPagesUpto(token, previousPage, session);
        int min = page.count() == 0 ? previousPagesTotal : previousPagesTotal + 1;

        page.setMin(min);
    }

    private Page filteredPage(DataAccessToken token, int pageNumber, Session session) throws Exception {
        PageReader reader = cache.reader(token);
        Page page = reader.page(pageNumber);
        ChangeSets changesets = cache.changesets(token, pageNumber, session);

        return filter.filter(page, changesets);
    }

    public int getPageCount(DataAccessToken token) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.totalPages();
    }

    public Page getPageWithRecord(DataAccessToken token, int record, Session session) throws Exception {
        int pageCount = cache.pageSize(token);
        int pageNumber = pageNumber(token, record, pageCount, session);
        return getPage(token, pageNumber, session);
    }

    int pageNumber(DataAccessToken token, int record, int pageCount, Session session) throws Exception {
        int pageSize = cache.pageSize(token);
        int low = 0;
        int high = low;
        for (int i = 1; i <= pageCount; i++) {
            ChangeSets sets = cache.changesets(token, i, session);
            int pageMax = pageSize + sets.netIncrease();
            high = low + pageMax;
            if ((low < record) && (record <= high))
                return i;

            low += pageMax;
        }

        return 0;
    }

    public int getTotalRecords(DataAccessToken token, Session session) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.totalRecords() + netRecordCountIncreaseDueToChanges(token, session);
    }

    private int netRecordCountIncreaseDueToChanges(DataAccessToken token, Session session) throws Exception {
        int total;
        ChangeSets changesets = cache.changesets(token, session);
        total = 0;
        for (ChangeSetsIterator iter = changesets.iterator(); iter.hasNext();) {
            ChangeSet element = iter.next();
            total += element.netIncrease();
        }

        return total;
    }

    public int defaultPageSize(Session session) {
        return cache.defaultPageSize(session);
    }

    int totalSizeOfPreviousPagesUpto(DataAccessToken token, int last, Session session) throws Exception {
        int result = 0;
        int pageSize = defaultPageSize(session);

        for (int i = 1; i <= last; i++) {
            ChangeSets sets = cache.changesets(token, i, session);
            result += pageSize + sets.netIncrease();
        }

        return result;
    }

}
