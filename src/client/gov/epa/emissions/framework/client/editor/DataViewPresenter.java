package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.Page;

public class DataViewPresenter {

    private DataEditorServices services;

    private DataView view;

    public DataViewPresenter(DataEditorServices services, DataView view) {
        this.services = services;
        this.view = view;
    }

    public void doDisplay(String table) throws EmfException {
        Page page = services.getPage(table, 0);
        view.display(page);
    }

}
