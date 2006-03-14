package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;

public interface NewNoteView {
    void display(User user, EmfDataset dataset, Version version, Note[] notes, NoteType[] types, Version[] versions);

    void display(User user, EmfDataset dataset, Note[] notes, NoteType[] types, Version[] versions);

    boolean shouldCreate();

    Note note();
}
