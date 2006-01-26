package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.framework.services.DataAccessToken;

import java.sql.SQLException;

import org.hibernate.Session;

public interface DataViewCache {

    PageReader reader(DataAccessToken token);

    void init(DataAccessToken token, Session session) throws SQLException;

    int defaultPageSize(Session session);

    int pageSize(DataAccessToken token);

    void init(DataAccessToken token, int pageSize, Session session) throws SQLException;

    void invalidate() throws SQLException;

    void reload(DataAccessToken token, Session session) throws SQLException;

    void close(DataAccessToken token, Session session) throws SQLException;

}