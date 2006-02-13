package gov.epa.emissions.framework.client.meta.logs;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingService;

public class LogsTabPresenter {

    private LogsTabView view;

    private LoggingService service;

    private EmfDataset dataset;

    public LogsTabPresenter(LogsTabView view, EmfDataset dataset, LoggingService services) {
        this.view = view;
        this.dataset = dataset;
        this.service = services;
    }

    public void doDisplay() throws EmfException {
        view.display(service.getAccessLogs(dataset.getId()));
    }

    public void doSave() {
        // No Op
    }

}
