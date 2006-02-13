package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.Keywords;

public interface EditableKeywordsTabPresenter {

    void display(Keywords masterKeywords);

    void doSave() throws EmfException;

}