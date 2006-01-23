package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.Keywords;

public interface EditableKeywordsTabView {
    void display(KeyVal[] values, Keywords masterKeywords);

    KeyVal[] updates() throws EmfException;
}
