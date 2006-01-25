package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class PageFetch {
    private Log LOG = LogFactory.getLog(PageFetch.class);

    private DataUpdatesCache cache;

    private HibernateSessionFactory sessionFactory;

    public PageFetch(DataUpdatesCache cache, HibernateSessionFactory sessionFactory) {
        this.cache = cache;
        this.sessionFactory = sessionFactory;
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        RecordsFilter filter = new RecordsFilter();

        try {
            PageReader reader = cache.reader(token);
            Page page = reader.page(pageNumber);
            Session session = sessionFactory.getSession();
            List changesets = cache.changesets(token, pageNumber, session);
            session.close();

            return filter.filter(page, changesets);
        } catch (Exception e) {
            LOG.error("Could not get Page: " + pageNumber + " for Dataset: " + token.datasetId() + ". Reason: " + e);
            throw new EmfException("Could not get Page: " + pageNumber + " for Dataset: " + token.datasetId());
        }

    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.totalPages();
        } catch (SQLException e) {
            LOG.error("Failed to get page count. Reason: " + e.getMessage());
            throw new EmfException("Failed to get page count");
        }
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.pageByRecord(record);
        } catch (SQLException ex) {
            LOG.error("Could not obtain the page with Record: " + record + ". Reason: " + ex.getMessage());
            throw new EmfException("Could not obtain the page with Record: " + record);
        }
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.totalRecords() + netRecordCountIncreaseDueToChanges(token);
        } catch (Exception e) {
            LOG.error("Failed to get a count of total number of records. Reason: " + e.getMessage());
            throw new EmfException("Failed to get a count of total number of records");
        }
    }

    private int netRecordCountIncreaseDueToChanges(DataAccessToken token) throws SQLException {
        Session session = sessionFactory.getSession();
        int total;
        try {
            List changesets = cache.changesets(token, session);
            total = 0;
            for (Iterator iter = changesets.iterator(); iter.hasNext();) {
                ChangeSet element = (ChangeSet) iter.next();
                total += element.netIncrease();
            }

            return total;
        } finally {
            session.close();
        }
    }

    public int defaultPageSize() {
        Session session = sessionFactory.getSession();
        int result = cache.defaultPageSize(session);
        session.close();

        return result;
    }
}
