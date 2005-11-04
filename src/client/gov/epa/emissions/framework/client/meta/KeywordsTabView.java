package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.client.data.MasterKeywords;

public interface KeywordsTabView {
    void display(KeyVal[] values, MasterKeywords keywords);

    KeyVal[] updates();
}
