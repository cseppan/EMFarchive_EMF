package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;

public interface NewNoteView {
    void display(User user, long datasetId, NoteType[] types, Version[] versions);

    boolean shouldCreate();

    Note note();
}
