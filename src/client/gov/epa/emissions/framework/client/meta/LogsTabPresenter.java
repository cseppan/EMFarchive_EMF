package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingServices;

public class LogsTabPresenter {

    private LogsTabView view;

    private LoggingServices services;

    private EmfDataset dataset;

    public LogsTabPresenter(LogsTabView view, EmfDataset dataset, LoggingServices services) {
        this.view = view;
        this.dataset = dataset;
        this.services = services;
    }

    public void display() throws EmfException {
        view.display(services.getAccessLogs(dataset.getDatasetid()));
    }

    public void save() {
        // No Op
    }

}
