package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Page {

    private List records;

    public Page() {
        records = new ArrayList();
    }

    public void add(Record record) {
        records.add(record);
    }

    public int count() {
        return records.size();
    }

    public boolean remove(int index) {
        return index < count() ? records.remove(index) != null : false;
    }

    public Record[] getRecords() {
        return (Record[]) records.toArray(new Record[0]);
    }

    public void setRecords(Record[] array) {
        records.clear();
        records.addAll(Arrays.asList(array));

    }

}
