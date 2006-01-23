package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.ui.RowSource;

public class EditableKeyValueRowSource implements RowSource {

    private KeyVal source;

    private Boolean selected;

    private Keywords masterKeywords;

    public EditableKeyValueRowSource(KeyVal source, Keywords masterKeywords) {
        this.source = source;
        this.masterKeywords = masterKeywords;
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
        return masterKeywords.get((String) val);
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) throws EmfException {
        Keyword keyword = source.getKeyword();
        if (keyword == null || keyword.getName().trim().length() == 0) {
            throw new EmfException("empty keyword at row "+rowNumber);
        }
        String value = source.getValue();
        if (value == null || value.trim().length() == 0) {
            throw new EmfException("empty value at row "+rowNumber);
        }
    }
}