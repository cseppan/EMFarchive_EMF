package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.framework.services.EditToken;

import java.util.List;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class DataEditorServiceCacheTest extends MockObjectTestCase {

    private DataEditorServiceCache cache;

    protected void setUp() throws Exception {
        super.setUp();

        Mock records = mock(ScrollableVersionedRecords.class);
        records.expects(atLeastOnce()).method("execute");
        records.expects(atLeastOnce()).method("close");

        Mock reader = mock(VersionedRecordsReader.class);
        reader.stubs().method("fetch").withAnyArguments().will(returnValue(records.proxy()));

        Mock writer = mock(VersionedRecordsWriter.class);
        writer.expects(once()).method("close");
        
        Mock writerFactory = mock(VersionedRecordsWriterFactory.class);
        writerFactory.stubs().method("create").withAnyArguments().will(returnValue(writer.proxy()));

        cache = new DataEditorServiceCache((VersionedRecordsReader) reader.proxy(),
                (VersionedRecordsWriterFactory) writerFactory.proxy(), null, null);
    }

    public void testShouldMaintainListOfChangeSetsPerPage() throws Exception {
        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token);

        ChangeSet changeset1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        cache.submitChangeSet(token, changeset2, 1);

        List results = cache.changesets(token, 1);
        assertEquals(2, results.size());
        assertEquals(changeset1, results.get(0));
        assertEquals(changeset2, results.get(1));

        cache.close(token);
    }

    public void testShouldMaintainSeparateChangeSetListsForEachPage() throws Exception {
        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token);

        ChangeSet changeset1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        cache.submitChangeSet(token, changeset2, 2);

        List resultsPage1 = cache.changesets(token, 1);
        assertEquals(1, resultsPage1.size());
        assertEquals(changeset1, resultsPage1.get(0));

        List resultsPage2 = cache.changesets(token, 2);
        assertEquals(1, resultsPage2.size());
        assertEquals(changeset2, resultsPage2.get(0));

        cache.close(token);
    }

    public void testShouldGetChangesetsForAllPagesByPage() throws Exception {
        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token);

        ChangeSet changeset1Page1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1Page1, 1);

        ChangeSet changesetPage2 = new ChangeSet();
        cache.submitChangeSet(token, changesetPage2, 2);

        ChangeSet changeset2Page1 = new ChangeSet();
        cache.submitChangeSet(token, changeset2Page1, 1);

        List all = cache.changesets(token);
        assertEquals(3, all.size());
        assertEquals(changeset1Page1, all.get(0));
        assertEquals(changeset2Page1, all.get(1));
        assertEquals(changesetPage2, all.get(2));

        cache.close(token);
    }

    public void testCloseShouldDiscardChangeSetsRelatedToEditToken() throws Exception {
        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token);

        ChangeSet changeset1Page1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1Page1, 1);

        List all = cache.changesets(token);
        assertEquals(1, all.size());

        cache.close(token);

        List empty = cache.changesets(token);
        assertEquals(0, empty.size());
    }

    public void testShouldDiscardChangeSetsOfAllPagesOnDiscard() throws Exception {
        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token);

        ChangeSet changeset1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        cache.submitChangeSet(token, changeset2, 2);

        cache.discardChangeSets(token);

        // empty
        List resultsPage1 = cache.changesets(token, 1);
        assertEquals(0, resultsPage1.size());
        List resultsPage2 = cache.changesets(token, 2);
        assertEquals(0, resultsPage2.size());

        cache.close(token);
    }

}
