package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;

import java.util.HashMap;
import java.util.Map;

public class DataViewPresenter {

    private DataView view;

    private Dataset dataset;

    private Map presenters;

    private DataEditorServices services;

    public DataViewPresenter(Dataset dataset, DataView view, DataEditorServices services) {
        this.dataset = dataset;
        this.view = view;
        this.services = services;

        presenters = new HashMap();
    }

    public void doDisplay() {
        view.observe(this);
        view.display(dataset);
    }

    public void doSelectTable(String table, PageView pageView) throws EmfException {
        if (presenters.containsKey(table)) {
            PageViewPresenter presenter = (PageViewPresenter) presenters.get(table);
            presenter.displayCurrent();
            return;
        }

        PageViewPresenter presenter = new PageViewPresenter(services, pageView, table);
        presenter.observeView(); // TODO: why this extra step?
        presenter.doDisplayFirst();// display first page
        presenters.put(table, presenter);
    }

    public void doClose() throws EmfException {
        services.close();
        view.close();
    }

}
