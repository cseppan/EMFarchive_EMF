package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

public class DataViewCacheImplTest extends MockObjectTestCase {

    public void testShouldReinitializeRecordsReaderOnApplyConstraints() throws Exception {
        Mock reader = mock(VersionedRecordsReader.class);
        DataViewCacheImpl cache = new DataViewCacheImpl((VersionedRecordsReader) reader.proxy());

        DataAccessToken token = new DataAccessToken();
        token.setVersion(new Version());
        token.setTable("table");

        String columnFilter = "col";
        String rowFilter = "row";
        String sortOrder = "sort";
        Session session = (Session) mock(Session.class).proxy();
        Constraint[] constraints = new Constraint[] { eq(token.getVersion()), eq(token.getTable()), eq(columnFilter),
                eq(rowFilter), eq(sortOrder), same(session) };
        
        Mock records = mock(ScrollableVersionedRecords.class);
        records.expects(once()).method("execute");
        
        reader.expects(once()).method("fetch").with(constraints).will(returnValue(records.proxy()));

        cache.init(token, 10, columnFilter, rowFilter, sortOrder, session);
    }
}
