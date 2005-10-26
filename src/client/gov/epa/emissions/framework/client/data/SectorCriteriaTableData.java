package gov.epa.emissions.framework.client.data;

import java.util.List;

import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;

public class SectorCriteriaTableData extends AbstractEmfTableData {

    public SectorCriteriaTableData(SectorCriteria[] criteria) {
    }

    public String[] columns() {
        return null;
    }

    public List rows() {
        return null;
    }

    public boolean isEditable(int col) {
        return false;
    }

}
