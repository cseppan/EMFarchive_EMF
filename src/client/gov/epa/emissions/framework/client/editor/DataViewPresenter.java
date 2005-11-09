package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;

public class DataViewPresenter {

    private DataView view;

    private Dataset dataset;

    public DataViewPresenter(Dataset dataset, DataView view) {
        this.dataset = dataset;
        this.view = view;
    }

    public void doDisplay() {
        view.display(dataset);
    }

    public void doSelectTable(String table, PageView pageView, DataEditorServices services) throws EmfException {
        PageViewPresenter presenter = new PageViewPresenter(services, pageView, table);
        presenter.doDisplayNext();
    }

}
