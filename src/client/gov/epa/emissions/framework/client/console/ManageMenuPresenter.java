package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.admin.UsersManagerPresenter;
import gov.epa.emissions.framework.client.admin.UsersManagerView;
import gov.epa.emissions.framework.client.casemanagement.CaseBrowserPresenter;
import gov.epa.emissions.framework.client.casemanagement.CaseBrowserView;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserView;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerPresenter;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerView;
import gov.epa.emissions.framework.client.data.sector.SectorsManagerPresenter;
import gov.epa.emissions.framework.client.data.sector.SectorsManagerView;
import gov.epa.emissions.framework.services.EmfException;

public class ManageMenuPresenter {

    private ManageMenuView view;

    private EmfSession session;

    public ManageMenuPresenter(ManageMenuView view, EmfSession session) {
        this.view = view;
        this.session = session;
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplayUserManager(UsersManagerView view) throws EmfException {
        UsersManagerPresenter presenter = new UsersManagerPresenter(session, session.userService());
        presenter.display(view);
    }

    public void doDisplayDatasetsBrowser(DatasetsBrowserView view) {
        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(session);
        presenter.doDisplay(view);
    }

    public void doDisplaySectors(SectorsManagerView view) throws EmfException {
        SectorsManagerPresenter presenter = new SectorsManagerPresenter(session, view, session.dataCommonsService());
        presenter.doDisplay();
    }

    public void doDisplayDatasetTypesManager(DatasetTypesManagerView view) throws EmfException {
        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(session, view);
        presenter.doDisplay();
    }

    public void doDisplayCases(CaseBrowserView view) throws EmfException {
        new CaseBrowserPresenter(session, view).display();
    }

}
