package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.WindowLayoutManager;

import java.util.HashMap;
import java.util.Map;

public class DatasetsBrowserPresenter {

    private DatasetsBrowserView view;

    private WindowLayoutManager windowLayoutManager;

    private DataServices dataServices;

    private Map editorsMap;

    public DatasetsBrowserPresenter(DataServices dataServices, WindowLayoutManager windowLayoutManager) {
        this.dataServices = dataServices;
        this.windowLayoutManager = windowLayoutManager;
        editorsMap = new HashMap();
    }

    public void doDisplay(DatasetsBrowserView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public void doClose() {
        view.close();
    }

    // FIXME: change other presenters to follow this design
    // Also, look at doShowMetadata to identify a better pattern
    public void doExport(ExportView exportView, ExportPresenter presenter, EmfDataset[] datasets) {
        if (datasets.length == 0) {
            view.showMessage("To Export, you will need to select at least one Dataset");
            return;
        }

        view.clearMessage();
        windowLayoutManager.add(exportView);

        presenter.display(exportView);
    }

    public void doRefresh() throws EmfException {
        // FIXME: fix the type casting
        view.refresh(dataServices.getDatasets());

        view.clearMessage();
    }

    // TODO: Is this a better style/pattern compared to doNew ?
    public void doShowProperties(PropertiesEditorView propertiesEditorView, EmfDataset dataset) {
        view.clearMessage();
        if (isPropertiesEditorAlive(dataset)) {
            propertiesEditor(dataset).bringToFront();
            return;
        }

        showPropertiesEditor(propertiesEditorView, dataset);
    }

    private boolean isPropertiesEditorAlive(EmfDataset dataset) {
        return editorsMap.containsKey(dataset) && propertiesEditor(dataset).isAlive();
    }

    private PropertiesEditorView propertiesEditor(EmfDataset dataset) {
        return (PropertiesEditorView) editorsMap.get(dataset);
    }

    private void showPropertiesEditor(PropertiesEditorView propertiesEditorView, EmfDataset dataset) {
        windowLayoutManager.add(propertiesEditorView);

        PropertiesEditorPresenter presenter = new PropertiesEditorPresenter(dataset, dataServices);
        presenter.display(propertiesEditorView);

        editorsMap.put(dataset, propertiesEditorView);
    }

    public void doNew(ImportView importView, ImportPresenter importPresenter) throws EmfException {
        view.clearMessage();
        windowLayoutManager.add(importView);

        importPresenter.display(importView);
    }

}
