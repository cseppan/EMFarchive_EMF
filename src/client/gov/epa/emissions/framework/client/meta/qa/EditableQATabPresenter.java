package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public interface EditableQATabPresenter extends PropertiesEditorTabPresenter {
    void display() throws EmfException;

    void doAddUsingTemplate(NewQAStepView stepview);

    void doSetStatus(SetQAStatusView statusview, QAStep[] steps);

    void doAddCustomized(NewCustomQAStepView view) throws EmfException;

    void doEdit(QAStep step, EditQAStepView performView, String versionName) throws EmfException;

}