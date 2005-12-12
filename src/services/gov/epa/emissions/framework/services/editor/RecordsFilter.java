package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecord;

public class RecordsFilter {

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
