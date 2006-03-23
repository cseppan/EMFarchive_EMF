package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;

public interface EditableQAStepsPresenter extends PropertiesEditorTabPresenter {
    void display() throws EmfException;

    void doAdd(NewQAStepView stepview);

}