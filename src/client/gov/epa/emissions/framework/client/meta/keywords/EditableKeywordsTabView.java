package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.services.EmfDataset;

public interface EditableKeywordsTabView {
    void display(EmfDataset dataset, Keywords masterKeywords);

    KeyVal[] updates() throws EmfException;
    
    void observeChanges(ChangeObserver listener);
}
