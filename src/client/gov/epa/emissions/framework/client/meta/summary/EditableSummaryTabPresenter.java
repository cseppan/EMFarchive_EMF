package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.framework.EmfException;

public interface EditableSummaryTabPresenter {

    public abstract void doSave() throws EmfException;

}