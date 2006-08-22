package gov.epa.emissions.framework.ui;

import java.util.HashMap;
import java.util.Map;

public class ViewableRow implements Row {
    private Map columns;

    private Object source;

    private RowSource rowSource;

    public ViewableRow(Object source, Object[] values) {
        this.source = source;

        columns = new HashMap();
        for (int i = 0; i < values.length; i++) {
            columns.put(new Integer(i), new Column(values[i]));
        }
    }

    public ViewableRow(RowSource rowSource) {
        this(rowSource.source(), rowSource.values());
        this.rowSource = rowSource;
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

    public Object source() {
        return source;
    }

    public RowSource rowSource() {
        return rowSource;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ViewableRow))
            return false;
        ViewableRow other = (ViewableRow) obj;
        return source.equals(other.source);

    }

    public int hashCode() {
        return source.hashCode();
    }

    public void setValueAt(Object value, int column) {
        // No Op
    }

}
