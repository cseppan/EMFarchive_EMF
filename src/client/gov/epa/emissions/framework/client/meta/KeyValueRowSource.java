package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.ui.RowSource;

public class KeyValueRowSource implements RowSource {

    private KeyVal source;

    private Boolean selected;

    private Keyword[] keywords;

    public KeyValueRowSource(KeyVal source, Keyword[] keywords) {
        this.source = source;
        this.keywords = keywords;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getKeyword().getName(), source.getValue() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            source.setKeyword(keyword(val));
            break;
        case 2:
            source.setValue((String) val);
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    private Keyword keyword(Object val) {
        for (int i = 0; i < keywords.length; i++) {
            if (keywords[i].getName().equals(val))
                return keywords[i];
        }

        return new Keyword((String) val);
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }
}