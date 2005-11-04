package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;

public interface KeywordsTabView {
    void display(KeyVal[] values, Keyword[] keywords);

    KeyVal[] updates();
}
