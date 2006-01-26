package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.services.editor.ChangeSets.ChangeSetsIterator;

public class RecordsFilter {

    public Page filter(Page page, ChangeSets changesets) {
        // TODO: efficiency is O(n^2). Need to optimize, but order should be maintained
        for (ChangeSetsIterator iter = changesets.iterator(); iter.hasNext();) {
            ChangeSet element = iter.next();
            page = filter(page, element);
        }

        return page;
    }

    public Page filter(Page page, ChangeSet changeset) {
        VersionedRecord[] results = filter(page.getRecords(), changeset);
        page.setRecords(results);

        return page;
    }

    public VersionedRecord[] filter(VersionedRecord[] records, ChangeSet changeset) {
        VersionedRecords results = new VersionedRecords();
        results.add(records);

        doNew(changeset, results);
        doDeleted(changeset, results);
        doUpdated(changeset, results);

        return results.get();
    }

    private void doNew(ChangeSet changeset, VersionedRecords results) {
        VersionedRecord[] newRecords = changeset.getNewRecords();
        results.add(newRecords);
    }

    private void doDeleted(ChangeSet changeset, VersionedRecords results) {
        VersionedRecord[] deleted = changeset.getDeletedRecords();
        for (int i = 0; i < deleted.length; i++)
            results.remove(deleted[i]);
    }

    private void doUpdated(ChangeSet changeset, VersionedRecords results) {
        VersionedRecord[] updated = changeset.getUpdatedRecords();
        for (int i = 0; i < updated.length; i++)
            results.replace(updated[i].getRecordId(), updated[i]);
    }

}
