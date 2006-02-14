package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;

public interface EditNotesTabView {

    void display(User user, Note[] notes, NoteType[] types, Version[] versions);

    Note[] additions();

}
