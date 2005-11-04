package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Keyword;

public class MasterKeywords {

    private Keyword[] keywords;

    public MasterKeywords(Keyword[] keywords) {
        this.keywords = keywords;
    }

    public Keyword get(String name) {
        name = name.trim();
        for (int i = 0; i < keywords.length; i++) {
            if (keywords[i].getName().equalsIgnoreCase(name))
                return keywords[i];
        }
        return new Keyword(name);
    }

    public Keyword[] all() {
        return keywords;
    }

}
