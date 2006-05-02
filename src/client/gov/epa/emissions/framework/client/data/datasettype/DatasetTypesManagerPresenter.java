package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class DatasetTypesManagerPresenter implements RefreshObserver {

    private DatasetTypesManagerView view;

    private EmfSession session;

    public DatasetTypesManagerPresenter(EmfSession session, DatasetTypesManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(service().getDatasetTypes());
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doClose() {
        view.disposeView();
    }

    public void doEdit(DatasetType type, EditableDatasetTypeView editable, ViewableDatasetTypeView viewable)
            throws EmfException {
        EditableDatasetTypePresenter p = new EditableDatasetTypePresenterImpl(session, editable, viewable, type);
        edit(p);
    }

    void edit(EditableDatasetTypePresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doView(DatasetType type, ViewableDatasetTypeView viewable) throws EmfException {
        ViewableDatasetTypePresenter p = new ViewableDatasetTypePresenterImpl(viewable, type);
        view(p);
    }

    void view(ViewableDatasetTypePresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void displayNewDatasetTypeView(NewDatasetTypeView view) {
        NewDatasetTypePresenter presenter = new NewDatasetTypePresenter(session, view);
        presenter.doDisplay();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getDatasetTypes());
    }

}
