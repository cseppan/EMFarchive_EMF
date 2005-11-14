package gov.epa.emissions.framework.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Page {

    private List records;

    public Page() {
        records = new ArrayList();
    }

    public void add(DbRecord record) {
        records.add(record);
    }

    public int count() {
        return records.size();
    }

    public boolean remove(int index) {
        return index < count() ? records.remove(index) != null : false;
    }

    public DbRecord[] getRecords() {
        return (DbRecord[]) records.toArray(new DbRecord[0]);
    }

    public void setRecords(DbRecord[] array) {
        records.clear();
        records.addAll(Arrays.asList(array));
    }

}
