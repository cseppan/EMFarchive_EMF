package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.ui.RowSource;

public class DatasetTypeKeywordRowSource implements RowSource {

    private Boolean selected;

    private Keyword keyword;

    public DatasetTypeKeywordRowSource(Keyword keyword) {
        this.keyword = keyword;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, keyword.getName() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            keyword.setName((String) val);
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return keyword;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }
}