package gov.epa.emissions.framework.ui;

import java.util.HashMap;
import java.util.Map;

public class EditableRow implements Row {
    private Map columns;

    private Object record;

    public EditableRow(Object record, Object[] values) {
        this.record = record;

        columns = new HashMap();
        for (int i = 0; i < values.length; i++) {
            columns.put(new Integer(i), new Column(values[i]));
        }
    }

    public Object getValueAt(int column) {
        Column columnHolder = (Column) columns.get(new Integer(column));
        return columnHolder.value;
    }

    private class Column {
        private Object value;

        public Column(Object value) {
            this.value = value;
        }
    }

    public Object record() {
        return record;
    }

}
