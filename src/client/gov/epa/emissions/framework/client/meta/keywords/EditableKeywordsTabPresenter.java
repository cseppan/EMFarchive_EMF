package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.services.EmfException;

public interface EditableKeywordsTabPresenter {

    void display(Keywords masterKeywords);

    void doSave() throws EmfException;

}