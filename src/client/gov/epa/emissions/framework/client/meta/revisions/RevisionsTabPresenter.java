package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

public class RevisionsTabPresenter {

    private EmfDataset dataset;

    private DataCommonsService service;

    public RevisionsTabPresenter(EmfDataset dataset, DataCommonsService service) {
        this.dataset = dataset;
        this.service = service;
    }

    public void display(RevisionsTabView view) throws EmfException {
        Revision[] revisions = service.getRevisions(dataset.getId());
        view.display(revisions, this);
    }

    public void doViewRevision(Revision revision, RevisionView view) {
        view.display(revision, dataset);
    }
}
