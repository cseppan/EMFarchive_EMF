package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;

public interface EditableQAStepsPresenter {

    public abstract void display() throws EmfException;

    public abstract void doSave() throws EmfException;

    public abstract void doAdd(NewQAStepView stepview);

}