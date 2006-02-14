package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Revision;

public class RevisionsTabPresenter {

    private EmfDataset dataset;

    private DataCommonsService service;

    public RevisionsTabPresenter(EmfDataset dataset, DataCommonsService service) {
        this.dataset = dataset;
        this.service = service;
    }

    public void display(RevisionsTabView view) throws EmfException {
        Revision[] revisions = service.getRevisions(dataset.getId());
        view.display(revisions);
    }
}
