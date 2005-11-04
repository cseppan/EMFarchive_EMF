package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Keyword;

public class Keywords {

    private Keyword[] keywords;

    public Keywords(Keyword[] keywords) {
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

    public boolean contains(String name) {
        for (int i = 0; i < keywords.length; i++) {
            if (keywords[i].getName().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

}
