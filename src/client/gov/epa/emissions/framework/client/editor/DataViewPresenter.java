package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

public class DataViewPresenter {

    private DataView view;

    private Dataset dataset;

    private DataEditorService services;

    public DataViewPresenter(Dataset dataset, DataView view, DataEditorService services) {
        this.dataset = dataset;
        this.view = view;
        this.services = services;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(dataset);
    }

    public void doSelectTable(String table, PageView pageView) throws EmfException {
        PageViewPresenter presenter = new PageViewPresenter(services, pageView, table);
        presenter.observeView(); // TODO: why this extra step?
        presenter.doDisplayFirst();// display first page
    }

    public void doClose() throws EmfException {
        services.close();
        view.close();
    }

}
