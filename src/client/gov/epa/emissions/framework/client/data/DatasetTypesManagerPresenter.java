package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class DatasetTypesManagerPresenter {

    private DatasetTypesManagerView view;

    private EmfSession session;

    public DatasetTypesManagerPresenter(EmfSession session, DatasetTypesManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(serviceLocator().dataCommonsService());
    }

    private ServiceLocator serviceLocator() {
        return session.serviceLocator();
    }

    public void doClose() {
        view.close();
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
    
}
