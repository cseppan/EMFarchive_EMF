package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EditToken;

import java.util.List;

import org.jmock.MockObjectTestCase;

public class DataEditorServiceCacheTest extends MockObjectTestCase {

    public void testShouldMaintainListOfChangeSetsPerPage() {
        DataEditorServiceCache cache = new DataEditorServiceCache(null, null, null);

        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        ChangeSet changeset1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        cache.submitChangeSet(token, changeset2, 1);

        List results = cache.changesets(token, 1);
        assertEquals(2, results.size());
        assertEquals(changeset1, results.get(0));
        assertEquals(changeset2, results.get(1));
    }

    public void testShouldMaintainSeparateChangeSetListsForEachPage() {
        DataEditorServiceCache cache = new DataEditorServiceCache(null, null, null);

        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

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
    }

    public void testShouldGetChangesetsForAllPagesByPage() {
        DataEditorServiceCache cache = new DataEditorServiceCache(null, null, null);

        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

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
    }

    public void testShouldDiscardChangeSetsOfAllPagesOnDiscard() {
        DataEditorServiceCache cache = new DataEditorServiceCache(null, null, null);

        EditToken token = new EditToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

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
    }

}
