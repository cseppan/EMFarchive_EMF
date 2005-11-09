package gov.epa.emissions.framework.client.editor;

import java.util.List;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;

public class PageTableData extends AbstractEmfTableData {

    private InternalSource source;
    private Page page;

    public PageTableData(InternalSource source, Page page) {
        this.source = source;
        this.page = page;
    }

    public String[] columns() {
        return source.getCols();
    }

    public List rows() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isEditable(int col) {
        return false;
    }

}
