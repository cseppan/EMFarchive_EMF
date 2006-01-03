package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.ui.ViewLayout;

public class DatasetTypesManagerPresenter {

    private DatasetTypesManagerView view;

    private ViewLayout viewLayout;

    private EmfSession session;

    public DatasetTypesManagerPresenter(EmfSession session, DatasetTypesManagerView view, ViewLayout layout) {
        this.session = session;
        this.view = view;
        this.viewLayout = layout;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(serviceLocator().datasetTypeService());
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
        edit(type, editable, p);
    }

    void edit(DatasetType type, EditableDatasetTypeView editable, EditableDatasetTypePresenter presenter)
            throws EmfException {
        if (viewLayout.activate("Edit" + type.getName()))
            return;

        viewLayout.add(editable, "Edit" + type.getName());
        presenter.doDisplay();
    }

    public void doView(DatasetType type, ViewableDatasetTypeView viewable) throws EmfException {
        ViewableDatasetTypePresenter p = new ViewableDatasetTypePresenterImpl(viewable, type, serviceLocator()
                .dataCommonsService());
        view(type, viewable, p);
    }

    void view(DatasetType type, ViewableDatasetTypeView viewable, ViewableDatasetTypePresenter presenter)
            throws EmfException {
        if (viewLayout.activate(type.getName()))
            return;

        viewLayout.add(viewable, type.getName());
        presenter.doDisplay();
    }
}
