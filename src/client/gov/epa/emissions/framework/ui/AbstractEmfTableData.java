package gov.epa.emissions.framework.ui;


import java.util.ArrayList;
import java.util.List;

//FIXME: TBT
public abstract class AbstractEmfTableData implements EmfTableData {

    public Object element(int row) {
        Row rowObj = (Row) rows().get(row);
        return rowObj.record();
    }

    public List elements(int[] rows) {
        List datasets = new ArrayList();
        for (int i = 0; i < rows.length; i++) {
            datasets.add(element(rows[i]));
        }

        return datasets;
    }
}
