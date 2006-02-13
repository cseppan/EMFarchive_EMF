package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;

public interface PropertiesEditorPresenter {

    void doDisplay(DatasetPropertiesEditorView view) throws EmfException;

    void doClose() throws EmfException;

    void doSave();

    void set(EditableSummaryTabView summary);

    void set(EditableKeywordsTabView keywordsView) throws EmfException;

    void onChange();

    void set(EditNotesTabView view) throws EmfException;

}