package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingService;

public class LogsTabPresenter {

    private LogsTabView view;

    private LoggingService services;

    private EmfDataset dataset;

    public LogsTabPresenter(LogsTabView view, EmfDataset dataset, LoggingService services) {
        this.view = view;
        this.dataset = dataset;
        this.services = services;
    }

    public void doDisplay() throws EmfException {
        view.display(services.getAccessLogs(dataset.getId()));
    }

    public void doSave() {
        // No Op
    }

}
