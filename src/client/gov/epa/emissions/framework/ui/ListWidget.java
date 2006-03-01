package gov.epa.emissions.framework.ui;

import java.util.Arrays;
import java.util.List;

import javax.swing.JList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

public class ListWidget extends JList {

    private Object[] items;

    public ListWidget(Object[] items) {
        super(items);
        this.items = items;
    }

    public ListWidget(Object[] items, Object[] selected) {
        this(items);
        setSelected(selected);
    }

    public void setSelected(Object[] selected) {
        List all = Arrays.asList(items);
        IntList indexes = new ArrayIntList();

        for (int i = 0; i < selected.length; i++)
            indexes.add(all.indexOf(selected[i]));

        super.setSelectedIndices(indexes.toArray());
    }

}
